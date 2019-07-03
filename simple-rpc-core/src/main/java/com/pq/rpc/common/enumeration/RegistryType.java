package com.pq.rpc.common.enumeration;

import com.pq.rpc.common.enumeration.support.ExtensionBaseType;
import com.pq.rpc.registry.api.ServiceRegistry;
import com.pq.rpc.registry.zookeeper.ZKServiceRegistry;

/**
 * 注册中心枚举类
 * 目前只实现了Zookeeper注册中心
 *
 * @author pengqi
 * create at 2019/7/3
 */
public enum RegistryType implements ExtensionBaseType<ServiceRegistry> {
    ZK(new ZKServiceRegistry());

    private ServiceRegistry serviceRegistry;

    RegistryType(ServiceRegistry serviceRegistry){
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public ServiceRegistry getInstance() {
        return serviceRegistry;
    }
}
