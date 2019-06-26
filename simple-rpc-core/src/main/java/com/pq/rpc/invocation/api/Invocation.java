package com.pq.rpc.invocation.api;

import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.protocol.api.InvokeParam;

import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * 远程调用方式的接口,具体有4种实现:
 * 1)同步调用
 * 2)异步调用
 * 3)oneWay调用
 * 4)回调调用
 *
 * @author pengqi
 * create at 2019/6/26
 */
public interface Invocation {
    /**
     * 参数为RPC请求和逻辑处理函数
     * 利用不同的调用方式来调用逻辑处理函数返回的Future对象,从而达到不同的调用效果
     * @param invokeParam 调用参数
     * @param requestProcessor 请求逻辑处理函数
     * @return 调用结果
     * @throws RPCException 异常
     */
    RPCResponse invoke(InvokeParam invokeParam, Function<RPCRequest, Future<RPCResponse>> requestProcessor) throws RPCException;

}
