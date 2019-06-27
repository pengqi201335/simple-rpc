package com.pq.rpc.transport.api.support;

import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.config.ServiceConfig;
import io.netty.channel.ChannelHandlerContext;

/**
 * 执行RPC任务的任务执行器
 *
 * @author pengqi
 * create at 2019/6/27
 */
public class RPCTaskRunner implements Runnable{

    private ServiceConfig serviceConfig;     //服务配置对象

    private ChannelHandlerContext ctx;      //RPC请求所在channel的ChannelHandlerContext对象,用于将RPCResponse刷出站

    public RPCTaskRunner(ServiceConfig serviceConfig,ChannelHandlerContext ctx){
        this.serviceConfig = serviceConfig;
        this.ctx = ctx;
    }

    @Override
    public void run() {

    }
}
