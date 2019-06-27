package com.pq.rpc.transport.api;

import com.pq.rpc.common.domain.RPCRequest;
import io.netty.channel.ChannelHandlerContext;

/**
 * 底层通信服务端接口
 *
 * @author pengqi
 * create at 2019/6/23
 */
public interface Server {

    /**
     * 运行服务端
     */
    void run();

    /**
     * 关闭服务端连接
     */
    void close();

    /**
     * RPC请求处理函数
     * @param request RPC请求
     * @param ctx 用于标记handler,以便任务在线程池中处理完之后能从正确的channel中被刷出
     */
    void handlerRPCRequest(RPCRequest request, ChannelHandlerContext ctx);
}
