package com.pq.rpc.cluster.support;

import com.pq.rpc.cluster.api.LoadBalancer;
import com.pq.rpc.config.GlobalConfig;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.protocol.api.Invoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象的负载均衡器
 * 模板方法模式
 *
 * @author pengqi
 * create at 2019/6/21
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    private GlobalConfig globalConfig;

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
}
