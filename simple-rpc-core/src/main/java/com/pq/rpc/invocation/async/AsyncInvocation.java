package com.pq.rpc.invocation.async;

import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.invocation.api.support.AbstractInvocation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * 异步调用方式
 *
 * @author pengqi
 * create at 2019/6/28
 */
public class AsyncInvocation extends AbstractInvocation {
    @Override
    protected RPCResponse doInvoke(RPCRequest request, ReferenceConfig referenceConfig, Function<RPCRequest, Future<RPCResponse>> requestProcessor) throws Throwable {
        Future<RPCResponse> future = requestProcessor.apply(request);
        //真正面向用户的Future对象
        Future<Object> resultFuture = new Future<Object>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return future.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }

            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public Object get() throws InterruptedException, ExecutionException {
                RPCResponse response = future.get();
                if(response.hasError()){
                    throw new ExecutionException(response.getErrorCause());
                }
                return response.getResult();
            }

            @Override
            public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                RPCResponse response = future.get(timeout,TimeUnit.MILLISECONDS);
                if(response.hasError()){
                    throw new ExecutionException(response.getErrorCause());
                }
                return response.getResult();
            }
        };
        //将此resultFuture设置为ThreadLocal变量,这样的话,提交请求的线程(用户)可以直接使用future对象,而不需要根据请求ID去查表
        return null;
    }
}
