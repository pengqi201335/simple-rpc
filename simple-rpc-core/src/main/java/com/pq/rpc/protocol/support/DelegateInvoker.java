package com.pq.rpc.protocol.support;

import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.protocol.api.InvokeParam;
import com.pq.rpc.protocol.api.Invoker;

/**
 * 封装了Filter链的Invoker对象
 * 提供一个抽象的invoke方法,用于操作Filter链
 *
 * @author pengqi
 * create at 2019/6/24
 */
public abstract class DelegateInvoker<T> extends AbstractInvoker<T>{
    private Invoker<T> delegate;

    public DelegateInvoker(Invoker<T> invoker){
        this.delegate = invoker;
    }

    public Invoker<T> getDelegate(){
        return delegate;
    }

    /* 用于处理 filter 链的抽象invoke方法 */
    public abstract RPCResponse invoke(InvokeParam invokeParam);
}
