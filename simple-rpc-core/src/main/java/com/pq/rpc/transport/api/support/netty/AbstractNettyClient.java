package com.pq.rpc.transport.api.support.netty;

import com.pq.rpc.common.context.RPCThreadSharedContext;
import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.config.ServiceConfig;
import com.pq.rpc.invocation.callback.CallbackInvocation;
import com.pq.rpc.transport.James.constance.JamesConstant;
import com.pq.rpc.transport.api.support.AbstractClient;
import com.pq.rpc.transport.api.support.RPCTaskRunner;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 基于netty的网络通信客户端,负责创建/关闭客户端连接并与netty服务端通信
 *
 * @author pengqi
 * create at 2019/6/24
 */
@Slf4j
public abstract class AbstractNettyClient extends AbstractClient {
    //客户端启动对象
    private Bootstrap bootstrap;
    //客户端的线程模型
    private NioEventLoopGroup group;

    //客户端连接管道
    private volatile Channel futureChannel;

    /**
     * 状态变量,判断当前客户端是否已经初始化,具有内存可见性,防止重复初始化
     */
    private volatile boolean initialized;

    /**
     * 状态变量,判断当前客户端是否已经关闭
     */
    private volatile boolean destroyed;

    /**
     * 状态变量,判断当前客户端连接关闭是否因为服务端下线,如果是的话,客户端不再重连
     */
    private volatile boolean closedByServer;

    private RetryExecutor retryExecutor = new RetryExecutor();

    //初始化ChannelPipeline,添加ChannelHandler,由具体客户端完成
    protected abstract ChannelInitializer initPipeline();

    /**
     * 连接远程节点
     * 设置为同步方法,因为可能有不同的服务调用者同时请求连接同一个服务器,所以需要做同步防止重复初始化
     */
    @Override
    protected synchronized void connect() {
        if(initialized){
            return;
        }
        //配置服务启动类
        this.group = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(initPipeline())
                .option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.TCP_NODELAY,true);
        //连接远程节点
        try{
            doConnect();
        }catch (Exception e){
            log.error("连接服务器失败...");
            e.printStackTrace();
            //尝试重连
            handlerException(e);
        }
    }

    private void doConnect() throws InterruptedException{
        String address = getServiceURL().getServiceAddress();
        String host = address.split(":")[0];
        Integer port = Integer.parseInt(address.split(":")[1]);
        //同步连接服务端
        ChannelFuture future = bootstrap.connect(host,port).sync();
        this.futureChannel = future.channel();
        log.info("客户端已连接至:"+address);
        log.info("初始化客户端完毕");
        initialized = true;
        destroyed = false;
    }

    /**
     * 异常逻辑处理器
     *
     * @param throwable 异常对象
     */
    @Override
    public void handlerException(Throwable throwable) {
        log.error("",throwable);
        log.info("开始尝试重新连接...");
        try{
            reconnect();
        }catch (Exception e){
            log.error("重连多次仍然失败,放弃重连");
            //关闭客户端连接
            close();
            throw new RPCException(ExceptionEnum.FAIL_TO_CONNECT_TO_SERVER,"FAIL_TO_CONNECT_TO_SERVER");
        }
    }

    /**
     * 重连
     */
    private void reconnect(){
        if(destroyed){
            retryExecutor.run();
        }
    }

    private class RetryExecutor implements Runnable{
        @Override
        public void run() {
            if(!closedByServer){
                //如果不是服务端主动关闭连接,就尝试重连
                try{
                    //先将原有的连接关闭
                    if(futureChannel!=null && futureChannel.isOpen()){
                        futureChannel.close().sync();
                    }
                    doConnect();
                }catch (Exception e){
                    //出现异常,反复重连
                    log.error("重连失败,{}秒后重试...", JamesConstant.IDLE_INTERVAL);
                    //调度执行重连方法
                    futureChannel.eventLoop().schedule(retryExecutor,JamesConstant.IDLE_INTERVAL, TimeUnit.SECONDS);
                }
            }else{
                //服务器主动关闭连接
                log.info("无法检测到该服务器,不再重连");
            }
        }
    }

    /**
     * 关闭客户端连接
     */
    @Override
    public void close() {
        try{
            if(futureChannel!=null && futureChannel.isOpen()){
                futureChannel.close().sync();
            }
            destroyed = true;
            closedByServer = true;
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            if(group!=null && !group.isShutdown() && !group.isTerminated()){
                //优雅停机
                group.shutdownGracefully();
            }
        }
    }

    /**
     * 客户端提交请求方法
     *
     * @param request RPC请求对象
     * @return Future对象(一个异步计算对象)
     */
    @Override
    public Future<RPCResponse> submit(RPCRequest request) {
        if(!initialized)
            connect();

        if(destroyed || closedByServer){
            throw new RPCException(ExceptionEnum.SUBMIT_AFTER_ENDPOINT_CLOSE,"SUBMIT_AFTER_ENDPOINT_CLOSE");
        }

        log.info("客户端发起调用请求:{},请求的服务器为:{}",request,getServiceURL().getServiceAddress());
        CompletableFuture<RPCResponse> responseFuture = new CompletableFuture<>();
        RPCThreadSharedContext.registryResponseFuture(request.getRequestID(),responseFuture);

        //将请求写入channel并出站
        this.futureChannel.writeAndFlush(request);

        log.info("请求已发送至:{}",getServiceURL().getServiceAddress());
        return responseFuture;
    }

    /**
     * 调用结果处理器
     *
     * @param response 远程调用结果对象
     */
    @Override
    public void handleRPCResponse(RPCResponse response) {
        //根据响应对象中的请求ID,找到对应的future对象
        CompletableFuture<RPCResponse> future = RPCThreadSharedContext.getAndRemoveResponseFuture(response.getRequestID());
        //将该future设置为已完成
        //1)同步调用:get()方法获取该response,并返回
        //2)异步调用:用户在需要的时候调用get()方法来获取response对象
        future.complete(response);
    }

    /**
     * 回调请求逻辑处理器
     * 此时客户端充当了服务器的角色,接收远程回调请求,并调用本地方法返回结果给远程回调请求
     *
     * @param request 回调请求对象
     * @param ctx     ChannelHandler对应的ChannelHandlerContext对象
     */
    @Override
    public void handleCallbackRequest(RPCRequest request, ChannelHandlerContext ctx) {
        ServiceConfig serviceConfig = RPCThreadSharedContext.getAndRemoveHandler(
                CallbackInvocation.generateCallbackHandlerKey(request,
                        ReferenceConfig.getReferenceConfigByInterfaceName(request.getInterfaceName()))
        );
        //将回调任务提交给线程池处理
        getGlobalConfig().getClientExecutor().submit(new RPCTaskRunner(ctx,request,serviceConfig));
    }

    @Override
    public boolean isAvailable() {
        return initialized && !destroyed;
    }
}
