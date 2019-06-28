package com.pq.rpc.invocation.oneway;

import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.invocation.api.support.AbstractInvocation;

import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * oneWay方式调用,即调用方不关心调用结果,提交完请求就直接返回null
 *
 * @author pengqi
 * create at 2019/6/28
 */
public class OneWayInvocation extends AbstractInvocation {
    @Override
    protected RPCResponse doInvoke(RPCRequest request, ReferenceConfig referenceConfig, Function<RPCRequest, Future<RPCResponse>> requestProcessor) throws Throwable {
        requestProcessor.apply(request);
        return null;
    }
}
