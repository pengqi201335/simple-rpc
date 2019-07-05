package com.pq.rpc.client.filter.impl;

import com.pq.rpc.common.context.RPCStatus;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.client.filter.Filter;
import com.pq.rpc.protocol.api.InvokeParam;
import com.pq.rpc.protocol.api.Invoker;
import lombok.extern.slf4j.Slf4j;

/**
 * 统计RPC调用活跃度的过滤器
 *
 * @author pengqi
 * create at 2019/7/1
 */
@Slf4j
public class ActivityStatisticsFilter implements Filter {
    /**
     * 过滤器invoke方法,实际上不处理业务逻辑,只是在真正的invoke前后增加一些前处理/后处理
     *
     * @param invoker     真正的业务逻辑invoker
     * @param invokeParam 调用参数
     * @return RPC调用结果
     */
    @Override
    public RPCResponse invoke(Invoker invoker, InvokeParam invokeParam) throws RPCException {
        RPCResponse response = null;
        try{
            log.info("start RPC...");
            //调用开始,活跃度+1
            RPCStatus.incActivity(
                    invokeParam.getInterfaceName(),
                    invokeParam.getMethodName(),
                    invoker.getServiceURL().getServiceAddress()
            );
            response = invoker.invoke(invokeParam);     //调用下一个filter或真正的invoker
        }catch (RPCException e){
            log.error("catch RPC exception...");
            //调用过程发生异常,活跃度-1
            RPCStatus.decActivity(
                    invokeParam.getInterfaceName(),
                    invokeParam.getMethodName(),
                    invoker.getServiceURL().getServiceAddress()
            );
            throw e;
        }
        log.info("finish RPC...");
        //调用完成,活跃度-1
        RPCStatus.decActivity(
                invokeParam.getInterfaceName(),
                invokeParam.getMethodName(),
                invoker.getServiceURL().getServiceAddress()
        );
        return response;
    }
}
