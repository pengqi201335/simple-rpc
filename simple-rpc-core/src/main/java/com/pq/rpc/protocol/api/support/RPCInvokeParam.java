package com.pq.rpc.protocol.api.support;

import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.protocol.api.InvokeParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装RPC请求对象和引用配置对象的类
 *
 * @author pengqi
 * create at 2019/6/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RPCInvokeParam implements InvokeParam {

    protected RPCRequest rpcRequest;

    protected ReferenceConfig referenceConfig;

    public RPCRequest getRPCRequest(){
        return rpcRequest;
    }

    @Override
    public String getInterfaceName() {
        return rpcRequest.getInterfaceName();
    }

    @Override
    public String getMethodName() {
        return rpcRequest.getMethodName();
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return rpcRequest.getParameterTypes();
    }

    @Override
    public Object[] getParameters() {
        return rpcRequest.getParameters();
    }

    @Override
    public String getRequestID() {
        return rpcRequest.getRequestID();
    }

    @Override
    public String toString(){
        return "RPCInvokeParam{"+
                rpcRequest+
                "}";
    }
}
