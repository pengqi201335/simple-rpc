package com.pq.rpc.protocol.support;

import com.pq.rpc.config.ServiceConfig;
import com.pq.rpc.protocol.api.Exporter;
import com.pq.rpc.protocol.api.Invoker;

/**
 * 抽象的Exporter对象
 * 封装:
 * 1)暴露之后的服务调用者
 * 2)该服务对应的ServiceConfig
 *
 * @author pengqi
 * create at 2019/6/23
 */
public abstract class AbstractExporter<T> implements Exporter<T> {

    protected Invoker<T> invoker;

    protected ServiceConfig<T> serviceConfig;

    public void setInvoker(Invoker<T> invoker) {
        this.invoker = invoker;
    }

    public void setServiceConfig(ServiceConfig<T> serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    @Override
    public Invoker<T> getInvoker() {
        return invoker;
    }

    @Override
    public ServiceConfig<T> getServiceConfig() {
        return serviceConfig;
    }
}
