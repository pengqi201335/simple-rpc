package com.pq.rpc.protocol.api;

import com.pq.rpc.config.ServiceConfig;

/**
 * 服务暴露之后的抽象调用接口,实质上就是将Invoker和ServiceConfig进行了封装
 *
 * @author pengqi
 * create at 2019/6/21
 */
public interface Exporter<T> {

    Invoker<T> getInvoker();

    ServiceConfig<T> getServiceConfig();
}
