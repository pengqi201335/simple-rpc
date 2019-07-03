package com.pq.rpc.common.enumeration;

import com.pq.rpc.cluster.api.LoadBalancer;
import com.pq.rpc.cluster.loadBalance.*;
import com.pq.rpc.common.enumeration.support.ExtensionBaseType;

/**
 * 负载均衡枚举类
 * 根据配置信息加载对应的负载均衡策略
 *
 * @author pengqi
 * create at 2019/7/3
 */
public enum LoadBalanceType implements ExtensionBaseType<LoadBalancer> {
    CONSISTENTHASH(new ConsistentHashLoadBalancer()),   //一致性哈希
    LEASTACTIVE(new LeastActiveLoadBalancer()),         //最小活跃度
    RANDOM(new RandomLoadBalancer()),                   //随机
    WEIGHTEDRANDOM(new WeightedRandomLoadBalancer()),   //加权随机
    ROUNDROBIN(new RoundRobinLoadBalancer());           //轮询

    private LoadBalancer loadBalancer;

    LoadBalanceType(LoadBalancer loadBalancer){
        this.loadBalancer = loadBalancer;
    }

    @Override
    public LoadBalancer getInstance() {
        return loadBalancer;
    }
}
