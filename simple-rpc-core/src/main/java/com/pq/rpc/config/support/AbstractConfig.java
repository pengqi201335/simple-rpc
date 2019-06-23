package com.pq.rpc.config.support;

import com.pq.rpc.config.*;

/**
 * 抽象配置类，维护一个GlobalConfig
 * GlobalConfig对象拥有以下实例
 * 1)ClusterConfig
 * 2)ProtocolConfig
 * 3)RegistryConfig
 * 4)ApplicationConfig
 *
 * @author pengqi
 * create at 2019/6/21
 */
public class AbstractConfig {

    private GlobalConfig globalConfig;

    public void init(GlobalConfig globalConfig){
        this.globalConfig = globalConfig;
    }

    public ClusterConfig getClusterConfig(){
        return globalConfig.getClusterConfig();
    }

    public RegistryConfig getRegistryConfig(){
        return globalConfig.getRegistryConfig();
    }

    public ProtocolConfig getProtocolConfig(){
        return globalConfig.getProtocolConfig();
    }

    public ApplicationConfig getApplicationConfig(){
        return globalConfig.getApplicationConfig();
    }
}
