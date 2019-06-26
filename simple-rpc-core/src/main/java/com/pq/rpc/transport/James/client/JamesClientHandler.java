package com.pq.rpc.transport.James.client;

import com.pq.rpc.common.domain.Message;
import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.transport.James.constance.JamesConstant;
import com.pq.rpc.transport.api.Client;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 面向James协议的客户端业务逻辑处理器
 *
 * @author pengqi
 * create at 2019/6/26
 */
@Slf4j
public class JamesClientHandler extends SimpleChannelInboundHandler<Message> {

    private Client client;  //客户端对象,因为要调用客户端的一些方法,所以要维护一个客户端实例

    private AtomicInteger timeoutCount;     //空闲超时计数器,客户端超过给定时间未读取到数据,计数器加1,收到数据计数器就置0

    //这里不能将此handler设置为单例,因为每个clientHandler都维护了一个超时计数器,每个client都有自己独立的timeoutCount,所以不能共享Handler
    JamesClientHandler(Client client){
        this.client = client;
    }

    /**
     * 读事件处理函数
     *
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        //先将计数器置0
        timeoutCount.set(0);
        if(message.getType()==Message.PONG){
            //收到的消息为服务器返回的PONG消息
            log.info("收到服务器{}PONG心跳",client.getServiceURL().getServiceAddress());
        }else if(message.getType()==Message.RESPONSE){
            //收到的消息为RPC调用响应,将其交给client的handleRPCResponse处理
            client.handleRPCResponse(message.getResponse());
        }else if(message.getType()==Message.REQUEST){
            //收到的消息为远程服务器的回调请求
            client.handleCallbackRequest(message.getRequest(),ctx);
        }
    }

    /**
     * 空闲事件处理器,当客户端在指定时间内没有收到消息,将会触发此方法
     *
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //先判断事件是否空闲事件
        if(evt instanceof IdleStateEvent){
            if(timeoutCount.getAndIncrement()>= JamesConstant.HEART_BEAT_TIMEOUT_MAX_TIMES){
                //超过心跳重试次数仍未收到消息,可以判断服务器宕机或应用不可用
                //关闭连接并重连
                client.handlerException(new RPCException(ExceptionEnum.HEART_BEAT_TIMES_EXCEED,"HEART_BEAT_TIMES_EXCEED"));
            }else{
                //还未超过心跳重连次数
                //发送心跳信号
                log.info("指定时间内为收到服务端消息,发送心跳信号至{}",client.getServiceURL().getServiceAddress());
                ctx.channel().writeAndFlush(Message.PING_MSG);
            }
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端出现异常");
        cause.printStackTrace();
        log.info("正在断开与服务器{}的连接...",client.getServiceURL().getServiceAddress());
        //出现异常直接断开重连
        client.handlerException(cause);
    }
}
