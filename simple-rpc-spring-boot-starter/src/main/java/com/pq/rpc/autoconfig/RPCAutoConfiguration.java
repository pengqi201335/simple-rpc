package com.pq.rpc.autoconfig;

import com.pq.rpc.autoconfig.beanPostProcessor.RPCConsumerBeanPostProcessor;
import com.pq.rpc.autoconfig.beanPostProcessor.RPCProviderBeanPostProcessor;
import com.pq.rpc.cluster.api.FaultToleranceHandler;
import com.pq.rpc.cluster.api.LoadBalancer;
import com.pq.rpc.cluster.api.support.AbstractLoadBalancer;
import com.pq.rpc.cluster.faultTolerance.FailOverFaultToleranceHandler;
import com.pq.rpc.cluster.loadBalance.LeastActiveLoadBalancer;
import com.pq.rpc.common.ExtensionLoader;
import com.pq.rpc.common.enumeration.*;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.config.*;
import com.pq.rpc.executor.api.TaskExecutor;
import com.pq.rpc.client.filter.Filter;
import com.pq.rpc.client.filter.impl.ActivityStatisticsFilter;
import com.pq.rpc.protocol.api.support.AbstractProtocol;
import com.pq.rpc.proxy.api.RPCProxyFactory;
import com.pq.rpc.registry.api.ServiceRegistry;
import com.pq.rpc.registry.zookeeper.ZKServiceRegistry;
import com.pq.rpc.serialize.api.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springboot自动配置类
 * simple-rpc-spring-boot-starter作为第三方依赖配置到项目中时,该类可以自动配置所有bean并注册到spring容器中供应用程序使用
 *
 * @author pengqi
 * create at 2019/7/4
 */
@EnableConfigurationProperties(RPCProperties.class)
@Configuration
@Slf4j
public class RPCAutoConfiguration implements InitializingBean {

    /**
     * 创建配置类/扩展点实例的工具类
     */
    private ExtensionLoader extensionLoader;

    @Autowired
    RPCProperties rpcProperties;

    @Bean(initMethod = "init",destroyMethod = "close")
    public RegistryConfig registryConfig(){
        RegistryConfig registryConfig = rpcProperties.getRegistryConfig();
        if(registryConfig==null){
            throw new RPCException(ExceptionEnum.SIMPLE_RPC_CONFIG_ERROR,"必须配置registry");
        }
        //注入依赖
        registryConfig.setServiceRegistryInstance(new ZKServiceRegistry(registryConfig));
        log.info("registryConfig:{}",registryConfig);
        return registryConfig;
    }

    @Bean
    public ApplicationConfig applicationConfig(){
        ApplicationConfig applicationConfig = rpcProperties.getApplicationConfig();
        if(applicationConfig==null){
            throw new RPCException(ExceptionEnum.SIMPLE_RPC_CONFIG_ERROR,"必须配置application");
        }
        //注入依赖
        applicationConfig.setRPCProxyFactoryInstance(extensionLoader.load(RPCProxyFactory.class, ProxyFactoryType.class,applicationConfig.getProxyFactoryName()));
        applicationConfig.setSerializerInstance(extensionLoader.load(Serializer.class, SerializeType.class,applicationConfig.getSerializer()));
        log.info("applicationConfig:{}",applicationConfig);
        return applicationConfig;
    }

    @Bean(destroyMethod = "close")
    public ProtocolConfig protocolConfig(ApplicationConfig applicationConfig, RegistryConfig registryConfig, ClusterConfig clusterConfig){
        ProtocolConfig protocolConfig = rpcProperties.getProtocolConfig();
        if(protocolConfig==null){
            throw new RPCException(ExceptionEnum.SIMPLE_RPC_CONFIG_ERROR,"必须配置protocol");
        }
        //注入依赖
        AbstractProtocol protocol = extensionLoader.load(AbstractProtocol.class,ProtocolType.class,protocolConfig.getType());
        //初始化protocol中的全局配置对象
        protocol.init(GlobalConfig.builder().
                protocolConfig(protocolConfig).
                applicationConfig(applicationConfig).
                registryConfig(registryConfig).
                clusterConfig(clusterConfig).
                build());
        protocolConfig.setProtocolInstance(protocol);
        ((AbstractLoadBalancer)clusterConfig.getLoadBalancerInstance()).updateGlobalConfig(GlobalConfig.
                builder().
                protocolConfig(protocolConfig).
                build());
        //配置Executor
        Executors executors = protocolConfig.getExecutors();
        if(executors!=null){
            ExecutorConfig clientExecutor = executors.getClient();
            if(clientExecutor!=null){
                TaskExecutor executor = extensionLoader.load(TaskExecutor.class,ExecutorType.class,clientExecutor.getType());
                executor.init(clientExecutor.getThreads());
                clientExecutor.setTaskExecutorInstance(executor);
            }
            ExecutorConfig serverExecutor = executors.getServer();
            if(serverExecutor!=null){
                TaskExecutor executor = extensionLoader.load(TaskExecutor.class,ExecutorType.class,serverExecutor.getType());
                executor.init(serverExecutor.getThreads());
                serverExecutor.setTaskExecutorInstance(executor);
            }
        }
        log.info("protocolConfig:{}",protocolConfig);
        return protocolConfig;
    }

    @Bean
    public ClusterConfig clusterConfig(ApplicationConfig applicationConfig,RegistryConfig registryConfig){
        ClusterConfig clusterConfig = rpcProperties.getClusterConfig();
        if(clusterConfig==null){
            throw new RPCException(ExceptionEnum.SIMPLE_RPC_CONFIG_ERROR,"必须配置cluster");
        }
        //注入依赖
        if(clusterConfig.getClusterFaultTolerance()!=null){
            clusterConfig.setFaultToleranceHandlerInstance(extensionLoader.load(
                    FaultToleranceHandler.class,
                    FaultToleranceHandlerType.class,
                    clusterConfig.getClusterFaultTolerance()
            ));
        }else{
            //默认的集群容错机制为failover
            clusterConfig.setFaultToleranceHandlerInstance(new FailOverFaultToleranceHandler());
        }

        clusterConfig.setLoadBalancerInstance(extensionLoader.load(
                LoadBalancer.class,
                LoadBalanceType.class,
                clusterConfig.getLoadBalance()
        ));

        ((AbstractLoadBalancer)clusterConfig.getLoadBalancerInstance()).updateGlobalConfig(GlobalConfig.builder().
                applicationConfig(applicationConfig).
                registryConfig(registryConfig).
                clusterConfig(clusterConfig).
                build());

        //如果负载均衡策略为最小活跃度算法,则要加载一个统计服务调用次数的filter
        if(clusterConfig.getLoadBalancerInstance() instanceof LeastActiveLoadBalancer){
            extensionLoader.registry(Filter.class,"leastActiveFilter",new ActivityStatisticsFilter());
        }

        log.info("clusterConfig:{}",clusterConfig);

        return clusterConfig;
    }

    @Bean
    public RPCConsumerBeanPostProcessor rpcConsumerBeanPostProcessor(ApplicationConfig applicationConfig,
                                                                     RegistryConfig registryConfig,
                                                                     ProtocolConfig protocolConfig,
                                                                     ClusterConfig clusterConfig){
        RPCConsumerBeanPostProcessor processor = new RPCConsumerBeanPostProcessor();
        processor.init(applicationConfig,registryConfig,protocolConfig,clusterConfig);
        log.info("客户端bean后置处理器初始化完成");
        return processor;
    }

    @Bean
    public RPCProviderBeanPostProcessor rpcProviderBeanPostProcessor(ApplicationConfig applicationConfig,
                                                                     RegistryConfig registryConfig,
                                                                     ProtocolConfig protocolConfig,
                                                                     ClusterConfig clusterConfig){
        RPCProviderBeanPostProcessor processor = new RPCProviderBeanPostProcessor();
        processor.init(applicationConfig,registryConfig,protocolConfig,clusterConfig);
        log.info("服务端bean后置处理器初始化完成");
        return processor;
    }

    @Override
    public void afterPropertiesSet() {
        this.extensionLoader = ExtensionLoader.getINSTANCE();
        //加载配置文件,实例化扩展点实现类并注册到一个map中
        extensionLoader.loadResources();
    }
}
