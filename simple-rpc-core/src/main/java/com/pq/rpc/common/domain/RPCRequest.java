package com.pq.rpc.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * RPC调用请求对象
 * 进行RPC调用时要被序列化,所以实现了Serializable接口
 *
 * @author pengqi
 * create at 2019/6/23
 */
@Data
public class RPCRequest implements Serializable {

    private String requestID;

    private String interfaceName;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] parameters;
}
