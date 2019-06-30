package com.pq.rpc.registry.api.support;

import com.pq.rpc.config.RegistryConfig;
import com.pq.rpc.registry.api.ServiceRegistry;

/**
 * 抽象的服务注册中心,只提供registry配置对象
 *
 * @author pengqi
 * create at 2019/6/29
 */
public abstract class AbstractServiceRegistry implements ServiceRegistry {

    protected RegistryConfig registryConfig;

    public void setRegistryConfig(RegistryConfig registryConfig){
        this.registryConfig = registryConfig;
    }
}
