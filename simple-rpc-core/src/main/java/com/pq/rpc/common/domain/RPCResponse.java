package com.pq.rpc.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * RPC调用结果，有以下几种调用场景
 * 1)provider端调用完成后，创建此对象，经序列化后发送给consumer
 * 2)consumer端接收到调用响应后，创建此对象，返回给用户
 *
 * @author pengqi
 * create at 2019/6/20
 *
 * //TODO Recycler
 */
@Data
public class RPCResponse implements Serializable {
    //请求编号
    private String requestID;

    //调用失败原因
    private Throwable errorCause;

    //调用结果
    private Object result;

    public boolean hasError(){
        return errorCause!=null;
    }
}
