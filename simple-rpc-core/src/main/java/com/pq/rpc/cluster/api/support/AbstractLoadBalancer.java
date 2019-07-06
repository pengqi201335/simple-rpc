package com.pq.rpc.cluster.api.support;

import com.pq.rpc.cluster.api.LoadBalancer;
import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.config.GlobalConfig;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.protocol.api.Invoker;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象的负载均衡器
 * 模板方法模式
 *
 * @author pengqi
 * create at 2019/6/21
 */
@Slf4j
public abstract class AbstractLoadBalancer implements LoadBalancer {

    private GlobalConfig globalConfig;

    public void updateGlobalConfig(GlobalConfig globalConfig){
        if(this.globalConfig==null){
            //用于clusterConfig
            this.globalConfig = globalConfig;
        }else{
            //用于protocolConfig,其实只需要配置ProtocolConfig
            if(this.globalConfig.getApplicationConfig()==null){
                this.globalConfig.setApplicationConfig(globalConfig.getApplicationConfig());
            }
            if(this.globalConfig.getRegistryConfig()==null){
                this.globalConfig.setRegistryConfig(globalConfig.getRegistryConfig());
            }
            if(this.globalConfig.getProtocolConfig()==null){
                this.globalConfig.setProtocolConfig(globalConfig.getProtocolConfig());
            }
            if(this.globalConfig.getClusterConfig()==null){
                this.globalConfig.setClusterConfig(globalConfig.getClusterConfig());
            }
        }
    }

    /**
     * 服务接口名与对应的clusterInvoker对象的映射表
     * k-v分别代表  k:服务接口名  v:与该服务唯一对应的clusterInvoker
     *
     */
    private Map<String , ClusterInvoker> interfaceInvokers = new ConcurrentHashMap<>();

    /**
     * 根据引用配置对象返回其对应的clusterInvoker
     * @param referenceConfig 引用配置对象
     *
     * @return ClusterInvoker对象
     */
    @SuppressWarnings("unchecked")
    public <T> Invoker<T> referCluster(ReferenceConfig<T> referenceConfig){
        String interfaceName = referenceConfig.getInterfaceName();  //服务接口名

        ClusterInvoker clusterInvoker;

        if(!interfaceInvokers.containsKey(interfaceName)){
            clusterInvoker = new ClusterInvoker(interfaceName,referenceConfig.getInterfaceClass(),globalConfig);
            interfaceInvokers.put(interfaceName,clusterInvoker);    //将新创建的clusterInvoker对象存入缓存中
            return clusterInvoker;
        }
        return interfaceInvokers.get(interfaceName);
    }

    /**
     * 利用某种负载均衡策略选择一个抽象服务调用者
     *
     * @param availableInvokers 可用服务列表
     * @return Invoker对象
     */
    @Override
    public Invoker select(List<Invoker> availableInvokers, RPCRequest request) {
        if(availableInvokers.size()==0){
            log.info("当前无可用服务:{}",request.getInterfaceName());
            return null;
        }
        Invoker invoker = doSelect(availableInvokers,request);
        log.info("LoadBalancer:{},choose invoker:{},requestID:{}",
                this.getClass().getSimpleName(),
                invoker.getServiceURL(),
                request.getRequestID());
        return invoker;
    }

    protected abstract Invoker doSelect(List<Invoker> availableInvokers,RPCRequest request);
}
