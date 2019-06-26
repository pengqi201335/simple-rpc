package com.pq.rpc.common.context;

import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.config.ServiceConfig;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储线程共享的对象,不使用ThreadLocal的原因是:发送请求的线程和接收响应的线程不一定相等
 */
public class RPCThreadSharedContext {

    private static final Map<String,CompletableFuture<RPCResponse>> RESPONSES = new ConcurrentHashMap<>();

    private static final Map<String, ServiceConfig> HANDLER_MAP = new ConcurrentHashMap<>();

    /* 服务端响应消息到了之后,调用此方法获取对应请求的future对象,设置调用成功,线程从get()返回 */
    public static CompletableFuture<RPCResponse> getAndRemoveResponseFuture(String requestID){
        return RESPONSES.remove(requestID);
    }

    /* 请求提交后,将对应的future对象注册到这里,供服务端响应消息处理器调用 */
    public static void registryResponseFuture(String requestID,CompletableFuture<RPCResponse> future){
        RESPONSES.put(requestID,future);
    }

    /* 服务端回调消息到了之后,调用此方法获取对应的回调ServiceConfig */
    public static ServiceConfig getAndRemoveHandler(String name){
        return HANDLER_MAP.remove(name);
    }

    /* 将即将被服务端回调的方法注册到这里,供服务端回调消息处理器调用 */
    public static void registryCallbackHandler(String name,ServiceConfig serviceConfig){
        HANDLER_MAP.put(name,serviceConfig);
    }
}
