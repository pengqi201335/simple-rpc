package com.pq.rpc.proxy.api;

import com.pq.rpc.protocol.api.Invoker;

/**
 * RPC代理对象工厂接口类
 * 1、当应用为客户端时，代理工厂负责生成远程服务的本地代理
 * 2、当应用为服务端时，代理工厂负责生成本地服务的本地代理
 *
 * @author pengqi
 * create at 2019/6/20
 */
public interface RPCProxyFactory {

    /**
     * 根据抽象调用者创建代理对象
     *
     * for consumer
     *
     * proxy调用服务时，实际上是通过invoker实现的，而invoker是protocol层的对象
     * 对客户端来说，invoker将通过远程通信来调用远程服务
     * 而对于服务端来说，invoker将直接调用本地服务
     *
     * @param invoker 调用者
     * @param <T>　
     * @return T 包装了invoker的代理对象
     */
    <T> T createProxy(Invoker<T> invoker);

    /**
     * 根据服务的本地代理及其类类型,创建一个对应的服务调用者
     *
     * for provider
     *
     * @param proxy 本地代理
     * @param clazz 代理对象类类型
     * @param <T> 代理对象类型
     * @return 本地服务的invoker
     */
    <T> Invoker<T> getInvoker(T proxy,Class<T> clazz);
}
