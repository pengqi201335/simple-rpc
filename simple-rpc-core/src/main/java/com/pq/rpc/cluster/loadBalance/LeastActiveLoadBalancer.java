package com.pq.rpc.cluster.loadBalance;

import com.pq.rpc.cluster.api.support.AbstractLoadBalancer;
import com.pq.rpc.common.context.RPCStatus;
import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.protocol.api.Invoker;

import java.util.List;

/**
 * 最小活跃度负载均衡算法
 *
 *
 * @author pengqi
 * create at 2019/7/1
 */
public class LeastActiveLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Invoker doSelect(List<Invoker> availableInvokers, RPCRequest request) {
        Invoker target = null;
        int leastActivity = 0;
        for(Invoker invoker:availableInvokers){
            int curActivity = RPCStatus.getActivity(request.getInterfaceName(),
                    request.getMethodName(),
                    invoker.getServiceURL().getServiceAddress());
            if(target==null || curActivity<leastActivity){
                //找到活跃度最小的invoker
                target = invoker;
                leastActivity = curActivity;
            }
        }
        return target;
    }
}
