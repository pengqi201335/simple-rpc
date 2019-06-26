package com.pq.rpc.transport.api.support;

import com.pq.rpc.config.GlobalConfig;
import com.pq.rpc.registry.api.ServiceURL;
import com.pq.rpc.transport.api.Client;

/**
 * 抽象客户端,主要做一些基础的配置工作供具体的客户端使用
 *
 * @author pengqi
 * create at 2019/6/24
 */
public abstract class AbstractClient implements Client {

    /**
     * 为客户端提供服务器地址
     */
    private ServiceURL serviceURL;

    /**
     * 提供用户的配置信息
     */
    private GlobalConfig globalConfig;

    public void init(ServiceURL serviceURL,GlobalConfig globalConfig){
        this.serviceURL = serviceURL;
        this.globalConfig = globalConfig;
        //与远程节点建立连接
        connect();
    }

    /* 由具体的通信框架实现的方法:与远程服务器节点建立连接 */
    protected abstract void connect();

    @Override
    public ServiceURL getServiceURL() {
        return serviceURL;
    }

    protected GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    /**
     * 更新配置
     *
     * @param serviceURL 新的配置信息
     */
    @Override
    public void updateServiceConfig(ServiceURL serviceURL) {
        this.serviceURL = serviceURL;
    }
}
