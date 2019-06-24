package com.pq.rpc.protocol.api;

import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.config.ServiceConfig;
import com.pq.rpc.registry.api.ServiceURL;

/**
 * 协议层的核心接口之一，定义以下方法
 * 1)refer() 引用远程服务
 * 2)export() 暴露服务
 * 3)referLocalService() 查找已经暴露的服务
 * 4)将当前客户端和服务端全部关闭
 *
 * 总的来说,Protocol就做两件事:
 * 1)管理Invoker对象(refer & export)
 * 2)管理客户端和服务端连接
 *
 * @author pengqi
 * create at 2019/6/21
 */
public interface Protocol {

    <T> Invoker<T> refer(ServiceURL serviceURL, ReferenceConfig<T> referenceConfig);

    <T> Exporter<T> export(Invoker<T> localInvoker, ServiceConfig<T> serviceConfig);

    <T> ServiceConfig<T> referLocalService(String interfaceName);

    void close();
}
