package com.pq.rpc.cluster.loadBalance;

import com.pq.rpc.cluster.api.support.AbstractLoadBalancer;
import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.protocol.api.Invoker;

import java.util.List;

/**
 * 轮询负载均衡算法
 *
 * @author pengqi
 * create at 2019/6/30
 */
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {
    private int index = 0;     //轮询负载均衡算法中,当前应当被选择的服务器索引

    @Override
    protected synchronized Invoker doSelect(List<Invoker> availableInvokers, RPCRequest request) {
        //定义为同步方法,保证index的正确性,防止多线程同时请求轮询时并发更新index
        Invoker invoker = availableInvokers.get(index);
        index = (index+1)%availableInvokers.size();
        return invoker;
    }

}
