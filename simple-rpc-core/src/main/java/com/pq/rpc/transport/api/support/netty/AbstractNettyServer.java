package com.pq.rpc.transport.api.support.netty;

import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.transport.api.support.AbstractServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 基于Netty的抽象服务端
 *
 * @author pengqi
 * create at 2019/6/27
 */
public abstract class AbstractNettyServer extends AbstractServer {

    private Channel futureChannel;

    private EventLoopGroup boss;
    private EventLoopGroup worker;



    @Override
    protected void doInit() {

    }

    protected abstract ChannelInitializer initPipeline();

    /**
     * 运行服务端
     */
    @Override
    public void run() {
        boss = new NioEventLoopGroup(1);    //boss线程组只分配一个线程
        worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(initPipeline())
                //配置属性
                .option()
                .option();
    }

    /**
     * 关闭服务端连接
     */
    @Override
    public void close() {

    }

    /**
     * RPC请求处理函数
     *
     * @param request RPC请求
     * @param ctx     用于标记handler,以便任务在线程池中处理完之后能从正确的channel中被刷出
     */
    @Override
    public void handlerRPCRequest(RPCRequest request, ChannelHandlerContext ctx) {
        getGlobalConfig().getServerExecutor().submit();
    }
}
