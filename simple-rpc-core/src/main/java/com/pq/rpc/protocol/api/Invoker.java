package com.pq.rpc.protocol.api;

import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.registry.api.ServiceURL;

/**
 * 抽象调用者
 * @param <T>
 *
 * @author pengqi
 * create at 2019/6/20
 */
public interface Invoker<T> {
    Class<T> getInterfaceClass();

    String getInterfaceName();

    ServiceURL getServiceURL();

    /**
     * 调用服务，返回调用结果
     * @param invokeParam 参数
     * @return RPCResponse对象
     */
    RPCResponse invoke(InvokeParam invokeParam) throws RPCException;

    boolean isAvailable();
}
