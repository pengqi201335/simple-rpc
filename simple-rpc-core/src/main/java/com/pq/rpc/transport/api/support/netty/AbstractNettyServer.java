package com.pq.rpc.transport.api.support.netty;

import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.transport.api.support.AbstractServer;
import com.pq.rpc.transport.api.support.RPCTaskRunner;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 基于Netty的抽象服务端
 *
 * @author pengqi
 * create at 2019/6/27
 */
@Slf4j
public abstract class AbstractNettyServer extends AbstractServer {

    private ChannelFuture channelFuture;

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
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(initPipeline())
                    //配置属性
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_BACKLOG,128)
                    //发送缓冲区大小
                    .option(ChannelOption.SO_SNDBUF,32*1024)
                    //接收缓冲区大小
                    .option(ChannelOption.SO_RCVBUF,32*1024)
                    .option(ChannelOption.TCP_NODELAY,true);
            //同步等待端口绑定完成
            this.channelFuture = serverBootstrap.bind(InetAddress.getLocalHost().getHostAddress(),getGlobalConfig().getPort()).sync();
            log.info("服务端启动,当前服务器类型:{}",this.getClass().getSimpleName());
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.getCause();
        }
    }

    /**
     * 关闭服务端连接
     */
    @Override
    public void close() {
        //关闭服务端持有的注册中心客户端
        getGlobalConfig().getRegistryConfig().close();
        if(boss!=null){
            boss.shutdownGracefully();
        }
        if(worker!=null){
            worker.shutdownGracefully();
        }
        if(channelFuture!=null){
            channelFuture.channel().close();
        }
    }

    /**
     * RPC请求处理函数
     *
     * @param request RPC请求
     * @param ctx     用于标记handler,以便任务在线程池中处理完之后能从正确的channel中被刷出
     */
    @Override
    public void handlerRPCRequest(RPCRequest request, ChannelHandlerContext ctx) {
        getGlobalConfig().getServerExecutor().submit(new RPCTaskRunner(
                ctx,
                request,
                getGlobalConfig().getProtocol().referLocalService(request.getInterfaceName())
                ));
    }
}
