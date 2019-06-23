package com.pq.rpc.protocol.api;

import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.config.ServiceConfig;
import com.pq.rpc.registry.api.ServiceURL;

/**
 * 协议层的核心接口之一，定义以下方法
 * 1)refer() 引用远程服务
 * 2)export() 暴露服务
 * 3)referLocalService() 引用本地服务
 *
 * @author pengqi
 * create at 2019/6/21
 *
 * TODO serviceConfig && referenceConfig
 */
public interface Protocol {

    <T> Invoker<T> refer(ServiceURL serviceURL, ReferenceConfig<T> referenceConfig);

    <T> Exporter<T> export(Invoker<T> localInvoker, ServiceConfig<T> serviceConfig);

    <T> T referLocalService(String interfaceName);
}
