package com.pq.rpc.proxy.api.support;

import com.pq.rpc.common.domain.GlobalRecycler;
import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.protocol.api.Invoker;
import com.pq.rpc.protocol.api.support.RPCInvokeParam;
import com.pq.rpc.proxy.api.RPCProxyFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象的动态代理工厂
 *
 * @author pengqi
 * create at 2019/6/28
 */
public abstract class AbstractProxyFactory implements RPCProxyFactory {
    //相同的接口复用同一个代理对象
    private Map<Class<?>,Object> PROXY_CACHE = new ConcurrentHashMap<>();

    //代理对象实际调用的接口方法
    protected Object invokeProxyMethod(Invoker invoker, Method method,Object[] args){
        Class<?>[] parameterTypes = method.getParameterTypes();     //参数类型列表
        String[] paramTypes = new String[parameterTypes.length];
        for(int i=0;i<paramTypes.length;i++){
            paramTypes[i] = parameterTypes[i].getName();
        }
        return invokeProxyMethod(invoker,method.getDeclaringClass().getName(),method.getName(),paramTypes,args);
    }

    private Object invokeProxyMethod(Invoker invoker,
                                     String interfaceName,
                                     String methodName,
                                     String[] parameterTypes,
                                     Object[] parameters){
        //处理一般方法
        if(methodName.equals("toString") && parameterTypes.length==0){
            return invoker.toString();
        }
        if(methodName.equals("hashCode") && parameterTypes.length==0){
            return invoker.hashCode();
        }
        if(methodName.equals("equals") && parameterTypes.length==1){
            return invoker.equals(parameters[0]);
        }
        //复用RPC请求对象
        RPCRequest request = GlobalRecycler.reuse(RPCRequest.class);
        //构造一个RPC请求对象
        //UUID生成一个随机编号
        request.setRequestID(UUID.randomUUID().toString());
        request.setInterfaceName(interfaceName);
        request.setMethodName(methodName);
        request.setParameterTypes(parameterTypes);
        request.setParameters(parameters);

        RPCInvokeParam rpcInvokeParam = RPCInvokeParam.builder()
                .rpcRequest(request)
                .referenceConfig(ReferenceConfig.getReferenceConfigByInterfaceName(interfaceName))
                .build();
        RPCResponse response = invoker.invoke(rpcInvokeParam);
        Object result = null;
        if(response!=null){
            //同步调用方式
            result = response.getResult();
        }
        return result;
    }

    /**
     * 根据抽象调用者创建代理对象
     * <p>
     * for consumer
     * <p>
     * proxy调用服务时，实际上是通过invoker实现的，而invoker是cluster层的对象
     * 对客户端来说，invoker将通过远程通信来调用远程服务
     * 而对于服务端来说，invoker将直接调用本地服务
     *
     * @param invoker 调用者
     * @return T 包装了invoker的代理对象
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Invoker<T> invoker) {
        if(PROXY_CACHE.containsKey(invoker.getInterfaceClass())){
            return (T)PROXY_CACHE.get(invoker.getInterfaceClass());
        }
        T t = doCreateProxy(invoker,invoker.getInterfaceClass());
        PROXY_CACHE.put(invoker.getInterfaceClass(),t);
        return t;
    }

    protected abstract <T> T doCreateProxy(Invoker<T> invoker,Class<T> interfaceClass);

    /**
     * 根据服务的本地代理及其类类型,创建一个对应的服务调用者
     * <p>
     * for provider
     *
     * @param proxy 本地代理
     * @param clazz 代理对象类类型
     * @return 本地服务的invoker
     */
    @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> clazz) {
        return null;
    }
}
