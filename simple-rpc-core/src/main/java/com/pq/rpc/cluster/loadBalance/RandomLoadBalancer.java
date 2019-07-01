package com.pq.rpc.cluster.loadBalance;

import com.pq.rpc.cluster.api.support.AbstractLoadBalancer;
import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.protocol.api.Invoker;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡算法
 *
 * @author pengqi
 * create at 2019/6/30
 */
public class RandomLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Invoker doSelect(List<Invoker> availableInvokers, RPCRequest request) {
        return availableInvokers.get(ThreadLocalRandom.current().nextInt(availableInvokers.size()));
    }
}
