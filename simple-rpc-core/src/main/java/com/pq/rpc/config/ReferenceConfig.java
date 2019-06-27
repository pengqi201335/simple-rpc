package com.pq.rpc.config;

import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.config.support.AbstractConfig;
import com.pq.rpc.filter.Filter;
import com.pq.rpc.protocol.api.Invoker;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程服务引用配置类，主要配置以下属性
 * 1)远程服务抽象调用者invoker
 * 2)远程服务的本地代理对象ref
 * 3)服务接口名
 * 4)服务接口类类型
 * 5)是否异步调用(默认同步调用)
 * 6)是否以oneWay(单向)的形式调用服务，即发送请求不等待应答，也没有回调函数触发，适用于"日志收集"等对可靠性要求不高、不需要返回值的场景
 * 7)是否以callback的形式来调用服务
 * 8)回调方法名
 * 9)回调参数索引，默认为1
 * 10)超时时间，默认为3000ms
 * 11)是否初始化(判断该referenceConfig对象是否已被初始化)
 * 12)是否泛化调用服务
 * 13)过滤器链表filters
 *
 */
@Data
@Builder
public class ReferenceConfig<T> extends AbstractConfig {

    /**
     * 加volatile关键字的原因是同一个服务的引用是有缓存的，可能多个线程同时引用了同一个服务
     * 那么它们就共享一个invoker，所以要保证内存可见性
     */
    private volatile Invoker<T> invoker;

    /**
     * 加volatile原因同上
     */
    private volatile T ref;

    private String interfaceName;

    private Class<T> interfaceClass;

    private boolean isAsync;

    private boolean isOneWay;

    private boolean isCallback;

    private String callbackMethod;

    private int callbackParamIndex = 1;     //TODO understand this attribute

    private long timeout = 3000;

    /**
     * 加volatile关键字的原因同上:只要invoker和ref被创建了就算是初始化了，所以该属性也必须具有内存可见性
     */
    private volatile boolean initialized;

    private boolean isGeneric;

    private List<Filter> filters;

    /**
     * 引用配置类的本地缓存，引用同一个服务时共享缓存中的同一个ReferenceConfig
     */
    private static final Map<String ,ReferenceConfig<?>> CACHE = new ConcurrentHashMap<>();

    /**
     * 创建一个引用配置对象
     * 为该配置对象配置好注解属性
     *
     */
    @SuppressWarnings("unchecked")
    public static <T> ReferenceConfig<T> createReferenceConfig(String interfaceName,
                                                               Class<T> interfaceClass,
                                                               boolean isAsync,
                                                               boolean isOneWay,
                                                               boolean isCallback,
                                                               String callbackMethod,
                                                               int callbackParamIndex,
                                                               long timeout,
                                                               boolean isGeneric,
                                                               List<Filter> filters){
        //缓存命中
        if(CACHE.containsKey(interfaceName)){
            //引用同一个服务的注解配置必须相同，否则缓存中的引用配置类的属性会覆盖注解属性，使其失效
            if(CACHE.get(interfaceName).hasDiff(isAsync,isOneWay,isCallback,callbackMethod,callbackParamIndex,timeout,isGeneric)){
                throw new RPCException(ExceptionEnum.SAME_SERVICE_CONNOT_BE_REFERRED_BY_DIFFERENT_CONFIG,
                        "同一个服务在同一个客户端只能以相同的配置被引用:"+interfaceName);
            }
            return (ReferenceConfig<T>) CACHE.get(interfaceName);
        }

        //缓存没有命中，即第一次配置此引用
        ReferenceConfig referenceConfig = ReferenceConfig.builder()
                .interfaceName(interfaceName)
                .interfaceClass((Class<Object>)interfaceClass)     //TODO understand why use (Class<Object>)interfaceClass to replace interfaceClass
                .isAsync(isAsync)
                .isOneWay(isOneWay)
                .isCallback(isCallback)
                .callbackMethod(callbackMethod)
                .callbackParamIndex(callbackParamIndex)
                .timeout(timeout)
                .filters(filters==null?new ArrayList<>():filters)
                .isGeneric(isGeneric).build();

        //放入缓存
        CACHE.put(interfaceName,referenceConfig);

        return referenceConfig;

    }

    /**
     * 判断引用同一个服务的注解配置是否不一致
     *
     */
    private boolean hasDiff(boolean isAsync,
                            boolean isOneWay,
                            boolean isCallback,
                            String callbackMethod,
                            int callbackParamIndex,
                            long timeout,
                            boolean isGeneric){

        if(this.isAsync!=isAsync)
            return true;
        if(this.isOneWay!=isOneWay)
            return true;
        if(this.isCallback!=isCallback)
            return true;
        if(this.isGeneric!=isGeneric)
            return true;
        if(!this.callbackMethod.equals(callbackMethod))
            return true;
        if(this.callbackParamIndex!=callbackParamIndex)
            return true;
        return this.timeout!=timeout;

    }

    /**
     * 初始化config
     */
    private void init(){
        if(initialized){
            return;
        }
        initialized = true;
        //获取ClusterInvoker,集群层面的Invoker
        invoker = getClusterConfig().getLoadBalancerInstance().referCluster(this);
        //根据ClusterInvoker调用代理工厂生产代理对象
        ref = getApplicationConfig().getRPCProxyFactoryInstance().createProxy(invoker);
    }

    /**
     * 获取引用服务的本地代理
     * 如果已经初始化，直接返回代理对象
     * 如果还未初始化，先初始化，再返回代理对象
     * @return 远程服务的本地代理对象(从这里可以看出，代理对象是类型为 T 的对象)
     */
    public T get(){
        if(!initialized){
            init();
        }
        return ref;
    }

    public static ReferenceConfig getReferenceConfigByInterfaceName(String interfaceName){
        return CACHE.get(interfaceName);
    }

    /**
     * 重写equals方法
     * TODO why rewrite equals()
     */
    @Override
    public boolean equals(Object o){
        if(o == this)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        ReferenceConfig<?> that = (ReferenceConfig<?>) o;
        return that.isAsync == this.isAsync &&
                that.isCallback == this.isCallback &&
                that.isOneWay == this.isOneWay &&
                that.isGeneric == this.isGeneric &&
                that.callbackMethod.equals(this.callbackMethod) &&
                that.callbackParamIndex == this.callbackParamIndex &&
                that.timeout == this.timeout &&
                that.interfaceName.equals(this.interfaceName) &&
                that.interfaceClass.equals(this.interfaceClass) &&
                that.initialized == this.initialized &&
                that.ref.equals(this.ref);

    }

    /**
     * 重写hashCode方法
     */
    @Override
    public int hashCode(){
        return Objects.hash(interfaceName,
                interfaceClass,
                isAsync,
                isCallback,
                isOneWay,
                isGeneric,
                callbackMethod,
                callbackParamIndex,
                timeout,
                initialized,
                ref);
    }

    @Override
    public String toString(){
        return "ReferenceConfig{" +
                "interfaceName:'" + interfaceName + '\'' +
                "interfaceClass:" + interfaceClass +
                "isAsync:" + isAsync +
                "isCallBack:" + isCallback +
                "isOneWay:" + isOneWay +
                "isGeneric:" + isGeneric +
                "callbackMethod:" + callbackMethod +
                "callbackParamIndex:" + callbackParamIndex +
                "timeout:" + timeout +
                "initialized:" + initialized +
                "ref:" + ref +
                "}";
    }
}
