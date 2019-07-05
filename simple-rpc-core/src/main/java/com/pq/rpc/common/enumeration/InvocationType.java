package com.pq.rpc.common.enumeration;

import com.pq.rpc.common.enumeration.support.ExtensionBaseType;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.invocation.api.Invocation;
import com.pq.rpc.invocation.async.AsyncInvocation;
import com.pq.rpc.invocation.callback.CallbackInvocation;
import com.pq.rpc.invocation.oneway.OneWayInvocation;
import com.pq.rpc.invocation.sync.SyncInvocaton;
import com.pq.rpc.protocol.api.InvokeParam;
import com.pq.rpc.protocol.api.support.RPCInvokeParam;

/**
 * 调用方式枚举类
 * 根据@RPCReference注解属性来选择枚举类
 *
 * @author pengqi
 * create at 2019/7/3
 */
public enum InvocationType{
    SYNC(new SyncInvocaton()),              //同步调用
    ASYNC(new AsyncInvocation()),           //异步调用
    CALLBACK(new CallbackInvocation()),     //回调方式
    ONEWAY(new OneWayInvocation());         //oneWay调用

    private Invocation invocation;

    InvocationType(Invocation invocation){
        this.invocation = invocation;
    }

    public static Invocation get(InvokeParam invokeParam) {
        ReferenceConfig referenceConfig = ((RPCInvokeParam)invokeParam).getReferenceConfig();
        if(referenceConfig.isAsync()){
            return ASYNC.invocation;
        }else if(referenceConfig.isCallback()){
            return CALLBACK.invocation;
        }else if(referenceConfig.isOneWay()){
            return ONEWAY.invocation;
        }else{
            return SYNC.invocation;
        }
    }
}
