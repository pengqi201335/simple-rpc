package com.pq.rpc.cluster.loadBalance;

import com.pq.rpc.cluster.api.support.AbstractLoadBalancer;
import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.protocol.api.Invoker;
import com.pq.rpc.registry.api.ServiceURL;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机负载均衡算法
 *
 * @author pengqi
 * create at 2019/7/1
 */
public class WeightedRandomLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Invoker doSelect(List<Invoker> availableInvokers, RPCRequest request) {
        int weightSum = 0;  //权重和
        //计算权重和
        for(Invoker invoker:availableInvokers){
            weightSum += Integer.parseInt(invoker.getServiceURL().getParamsByKey(ServiceURL.Key.WEIGHT).get(0));
        }
        int randomValue = ThreadLocalRandom.current().nextInt(weightSum);   //随机得到一个[0,weightSum)之间的值
        for(Invoker invoker:availableInvokers){
            //得到当前invoker的权重
            int currentWeight = Integer.parseInt(invoker.getServiceURL().getParamsByKey(ServiceURL.Key.WEIGHT).get(0));
            randomValue -= currentWeight;
            if(randomValue<0){
                return invoker;
            }
        }
        return null;
    }
}
