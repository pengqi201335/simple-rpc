package com.pq.rpc.invocation.sync;

import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.invocation.api.support.AbstractInvocation;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 同步调用方式
 *
 * @author pengqi
 * create at 2019/6/28
 */
@Slf4j
public class SyncInvocaton extends AbstractInvocation {
    @Override
    protected RPCResponse doInvoke(RPCRequest request, ReferenceConfig referenceConfig, Function<RPCRequest, Future<RPCResponse>> requestProcessor) throws Throwable {
        Future<RPCResponse> future = requestProcessor.apply(request);   //直接提交请求
        //get()方法使得线程阻塞在此
        RPCResponse response = future.get(referenceConfig.getTimeout(),TimeUnit.MILLISECONDS);
        log.info("收到RPC调用结果:"+response);
        return response;
    }
}
