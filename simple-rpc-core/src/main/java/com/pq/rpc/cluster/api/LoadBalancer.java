package com.pq.rpc.cluster.api;

import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.protocol.api.Invoker;

import java.util.List;

/**
 * 负载均衡器
 * 利用指定的负载均衡策略，在服务列表中选择一个
 *
 * @author pengqi
 * create at 2019/6/20
 */
public interface LoadBalancer {
    /**
     * 根据referenceConfig创建一个对应的ClusterInvoker
     * Lakers championship!
     */
    <T> Invoker<T> referCluster(ReferenceConfig<T> referenceConfig);

    /**
     * 利用某种负载均衡策略选择一个抽象服务调用者
     * @param availableInvokers 可用服务列表
     *
     * @return Invoker对象
     */
    Invoker select(List<Invoker> availableInvokers, RPCRequest request);
}
