package com.pq.rpc.common.enumeration;

import com.pq.rpc.common.enumeration.support.ExtensionBaseType;
import com.pq.rpc.invocation.api.Invocation;
import com.pq.rpc.invocation.async.AsyncInvocation;
import com.pq.rpc.invocation.callback.CallbackInvocation;
import com.pq.rpc.invocation.oneway.OneWayInvocation;
import com.pq.rpc.invocation.sync.SyncInvocaton;

/**
 * 调用方式枚举类
 * 根据配置信息选择加载对应的调用方式
 *
 * @author pengqi
 * create at 2019/7/3
 */
public enum InvocationType implements ExtensionBaseType<Invocation> {
    SYNC(new SyncInvocaton()),              //同步调用
    ASYNC(new AsyncInvocation()),           //异步调用
    CALLBACK(new CallbackInvocation()),     //回调方式
    ONEWAY(new OneWayInvocation());         //oneWay调用

    private Invocation invocation;

    InvocationType(Invocation invocation){
        this.invocation = invocation;
    }

    @Override
    public Invocation getInstance() {
        return invocation;
    }
}
