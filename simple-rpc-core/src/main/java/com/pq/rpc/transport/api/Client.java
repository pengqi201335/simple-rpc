package com.pq.rpc.transport.api;

import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.registry.api.ServiceURL;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Future;

/**
 * 底层通信客户端接口
 *
 * @author pengqi
 * create at 2019/6/23
 */
public interface Client {

    /**
     * 客户端提交请求方法
     * @return Future对象(一个异步计算对象)
     */
    Future<RPCResponse> submit(RPCRequest request);

    /**
     * 关闭客户端连接
     */
    void close();

    /**
     * 异常逻辑处理器
     */
    void handlerException(Throwable throwable);

    /**
     * 调用结果处理器
     * @param response 远程调用结果对象
     */
    void handleRPCResponse(RPCResponse response);

    /**
     * 回调请求逻辑处理器
     * @param request 回调请求对象
     * @param ctx ChannelHandler对应的ChannelHandlerContext对象
     */
    void handleCallbackRequest(RPCRequest request, ChannelHandlerContext ctx);

    /**
     * 更新配置
     * @param serviceURL 新的配置信息
     */
    void updateEndPointConfig(ServiceURL serviceURL);

    /* 获取与该客户端实例连接的服务信息 */
    ServiceURL getServiceURL();

    boolean isAvailable();
}
