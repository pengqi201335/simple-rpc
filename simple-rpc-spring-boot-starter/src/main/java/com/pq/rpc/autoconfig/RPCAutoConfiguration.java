package com.pq.rpc.autoconfig;

import com.pq.rpc.common.ExtensionLoader;
import com.pq.rpc.common.enumeration.*;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.config.ApplicationConfig;
import com.pq.rpc.config.ProtocolConfig;
import com.pq.rpc.config.RegistryConfig;
import com.pq.rpc.executor.api.TaskExecutor;
import com.pq.rpc.protocol.api.Protocol;
import com.pq.rpc.proxy.api.RPCProxyFactory;
import com.pq.rpc.registry.api.ServiceRegistry;
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

    @Bean
    public RegistryConfig registryConfig(){
        RegistryConfig registryConfig = rpcProperties.getRegistryConfig();
        if(registryConfig==null){
            throw new RPCException(ExceptionEnum.SIMPLE_RPC_CONFIG_ERROR,"必须配置registry");
        }
        //注入依赖
        registryConfig.setServiceRegistryInstance(extensionLoader.load(ServiceRegistry.class, RegistryType.class,registryConfig.getType()));
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

    @Bean
    public ProtocolConfig protocolConfig(){
        ProtocolConfig protocolConfig = rpcProperties.getProtocolConfig();
        if(protocolConfig==null){
            throw new RPCException(ExceptionEnum.SIMPLE_RPC_CONFIG_ERROR,"必须配置protocol");
        }
        //TODO 注入依赖
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.extensionLoader = ExtensionLoader.getINSTANCE();
        //加载配置文件,实例化扩展点实现类并注册到一个map中
        extensionLoader.loadResources();
    }
}
