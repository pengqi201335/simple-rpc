package com.pq.rpc.invocation.callback;

import com.pq.rpc.common.context.RPCThreadSharedContext;
import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.config.ServiceConfig;
import com.pq.rpc.invocation.api.support.AbstractInvocation;

import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * 以回调的方式请求RPC服务
 *
 * @author pengqi
 * create at 2019/6/28
 */
public class CallbackInvocation extends AbstractInvocation {
    @Override
    protected RPCResponse doInvoke(RPCRequest request, ReferenceConfig referenceConfig, Function<RPCRequest, Future<RPCResponse>> requestProcessor) throws Throwable {
        // 先获取callback实例
        Object callbackInstance = request.getParameters()[referenceConfig.getCallbackParamIndex()];
        request.getParameters()[referenceConfig.getCallbackParamIndex()] = null;
        registryCallbackHandler(request,referenceConfig,callbackInstance);
        requestProcessor.apply(request);    //提交请求
        return null;
    }

    /**
     * callback实例注册函数,将callback实例注册到一个线程共享的全局map,回调线程根据RPC请求编号取callback实例来执行
     * @param request RPC请求对象
     * @param referenceConfig 引用配置类
     * @param callback callback实例
     */
    @SuppressWarnings("unchecked")
    private void registryCallbackHandler(RPCRequest request,ReferenceConfig referenceConfig,Object callback){
        Class<?> interfaceClass = callback.getClass().getInterfaces()[0];

        //生成一个ServiceConfig对象
        ServiceConfig config = ServiceConfig.builder()
                .interfaceClass((Class<Object>)interfaceClass)
                .interfaceName(interfaceClass.getName())
                .isCallbackInterface(true)
                .ref(callback)
                .build();

        RPCThreadSharedContext.registryCallbackHandler(generateCallbackHandlerKey(request,referenceConfig),config);
    }

    /**
     * 根据RPC请求对象和引用配置对象生成一个callback实例的键
     */
    public static String generateCallbackHandlerKey(RPCRequest request,ReferenceConfig referenceConfig){
        return request.getRequestID()+"."+request.getParameterTypes()[referenceConfig.getCallbackParamIndex()];
    }

    /**
     * 根据RPC请求对象生成一个callback实例的键
     */
    public static String generateCallbackHandlerKey(RPCRequest request){
        return request.getRequestID()+"."+request.getInterfaceName();
    }
}
