package com.pq.rpc.protocol.api.support;

import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.enumeration.InvocationType;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.config.GlobalConfig;
import com.pq.rpc.client.filter.Filter;
import com.pq.rpc.protocol.api.InvokeParam;
import com.pq.rpc.protocol.api.Invoker;
import com.pq.rpc.registry.api.ServiceURL;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 抽象调用者,实现了Invoker的绝大部分逻辑,包括:
 *
 * 1)invoke()方法的大部分逻辑,具体invoker只需要重写getProcessor()方法来调用客户端submit一下请求,
 *   然后利用返回的Future对象来进行方法调用
 *
 * 2)buildFilterChain()方法用于构建filter链,其返回的是一个InvokerDelegate对象,这也是返回给ClusterInvoker的对象
 *
 * @author pengqi
 * create at 2019/6/24
 */
@Slf4j
public abstract class AbstractInvoker<T> implements Invoker<T> {

    private String interfaceName;

    private Class<T> interfaceClass;

    private GlobalConfig globalConfig;

    @Override
    public Class<T> getInterfaceClass() {
        return interfaceClass;
    }

    @Override
    public String getInterfaceName() {
        return interfaceName;
    }

    @Override
    public ServiceURL getServiceURL() {
        return ServiceURL.DEFAULT_SERVICE_URL;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public void setInterfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    /**
     * 调用服务，返回调用结果
     *
     * @param invokeParam 参数
     * @return RPCResponse对象
     */
    @Override
    public RPCResponse invoke(InvokeParam invokeParam) throws RPCException {
        Function<RPCRequest,Future<RPCResponse>> logic = getProcessor();
        if(logic==null){
            throw new RPCException(ExceptionEnum.GET_PROCESSOR_METHOD_MUST_BE_OVERRIDE,"GET_PROCESSOR_METHOD_MUST_BE_OVERRIDE");
        }
        //根据用户的配置选择具体的调用方法并进行远程调用
        return InvocationType.get(invokeParam).invoke(invokeParam,logic);
    }

    /**
     * 函数式编程,返回一个函数对象
     * 该函数的输入为RPC调用请求,输出为计算RPC调用结果的Future对象
     * 由具体的invoker来重写此方法,实现函数的内部逻辑
     * @return Function
     */
    protected Function<RPCRequest,Future<RPCResponse>> getProcessor(){
        return null;
    }

    /**
     * 根据用户配置的过滤器列表创建Filter链,在没有到达Filter链末端时,不会真正调用invoke方法,只是做一些前处理
     * @param filters 用户配置的filter列表
     * @return 一个包装了filter链的invoker,抽象为一个DelegateInvoker对象
     */
    public Invoker<T> buildFilterChain(List<Filter> filters){
        //refer()方法真正返回的Invoker对象,亦即服务发现时protocol返回给CLusterInvoker的Invoker对象
        return new DelegateInvoker<>(this){
            private ThreadLocal<AtomicInteger> filterIndex = new ThreadLocal<>(){
                @Override
                protected AtomicInteger initialValue() {
                    return new AtomicInteger(0);
                }
            };

            @Override
            public RPCResponse invoke(InvokeParam invokeParam) {
                //打印当前线程的filter索引位置
                log.info("filterIndex:"+filterIndex.get().get()+"invokeParam:"+invokeParam);
                final Invoker<T> invokerWrappedFilters = this;
                //还未扫描完整条过滤链
                if(filterIndex.get().get()<filters.size()){
                    //从过滤链中取出一个,并将索引原子自增1,调用过滤器的invoke方法进行前处理
                    return filters.get(filterIndex.get().getAndIncrement()).invoke(new AbstractInvoker() {
                        @Override
                        public Class getInterfaceClass() {
                            return getDelegate().getInterfaceClass();
                        }

                        @Override
                        public String getInterfaceName() {
                            return getDelegate().getInterfaceName();
                        }

                        @Override
                        public ServiceURL getServiceURL() {
                            return getDelegate().getServiceURL();
                        }

                        /**
                         * 调用服务，返回调用结果,过滤器前处理完成后调用此方法
                         *
                         * @param invokeParam 参数
                         * @return RPCResponse对象
                         */
                        @Override
                        public RPCResponse invoke(InvokeParam invokeParam) throws RPCException {
                            return invokerWrappedFilters.invoke(invokeParam);
                        }
                    },invokeParam);
                }
                //扫描完整条过滤连,此时已经做完所有前处理,可以调用真正的invoker执行调用逻辑
                //调用之前先将filterIndex置零   //TODO why
                filterIndex.get().set(0);
                //这里是调用delegate内部的AbstractInvoker对象的invoke()方法
                return getDelegate().invoke(invokeParam);
            }
        };
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
