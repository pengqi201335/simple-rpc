package com.pq.rpc.proxy.jdk;

import com.pq.rpc.protocol.api.Invoker;
import com.pq.rpc.proxy.api.support.AbstractProxyFactory;

import java.lang.reflect.Proxy;

/**
 * JDK动态代理实现的代理工厂
 *
 * @author pengqi
 * create at 2019/6/28
 */
public class JDKProxyFactory extends AbstractProxyFactory {
    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(Invoker<T> invoker, Class<T> interfaceClass) {
        //生成代理对象
        return (T)Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                (proxy, method, args) ->
                    JDKProxyFactory.this.invokeProxyMethod(invoker,method,args)
        );
    }
}
