package com.pq.rpc.transport.api.support;

import com.pq.rpc.config.GlobalConfig;
import com.pq.rpc.transport.api.Server;

/**
 * 抽象的服务端
 *
 * @author pengqi
 * create at 2019/6/27
 */
public abstract class AbstractServer implements Server {

    private GlobalConfig globalConfig;  //全局配置类,必不可少

    public void init(GlobalConfig globalConfig){
        this.globalConfig = globalConfig;
        doInit();
    }

    protected GlobalConfig getGlobalConfig(){
        return globalConfig;
    }

    protected abstract void doInit();
}
