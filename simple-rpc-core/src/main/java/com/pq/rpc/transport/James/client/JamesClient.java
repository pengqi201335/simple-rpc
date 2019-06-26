package com.pq.rpc.transport.James.client;

import com.pq.rpc.transport.James.codec.JamesDecoder;
import com.pq.rpc.transport.James.codec.JamesEncoder;
import com.pq.rpc.transport.James.constance.JamesConstant;
import com.pq.rpc.transport.api.support.netty.AbstractNettyClient;
import com.pq.rpc.transport.constance.CommunicationConstant;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 面向James协议的客户端
 * 不同协议只需要用不同的handler来处理,所以面向具体协议的客户端只需要编写自己的pipeline
 *
 * @author pengqi
 * create at 2019/6/26
 */
public class JamesClient extends AbstractNettyClient {
    @Override
    protected ChannelInitializer initPipeline() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) {
                channel.pipeline()
                        //空闲检测逻辑处理器
                        .addLast("IdleStateHandler", new IdleStateHandler(0, JamesConstant.IDLE_INTERVAL,0))
                        //在消息头部加上消息长度字段
                        .addLast("LengthFieldPrepender",new LengthFieldPrepender(CommunicationConstant.LENGTH_FIELD_LENGTH,CommunicationConstant.LENGTH_ADJUSTMENT))
                        //编码器
                        .addLast("JamesEncoder",JamesEncoder.getInstance(getGlobalConfig().getSerializer()))
                        //基于长度域的拆包器,解决粘包/半包问题
                        .addLast("LengthFieldBasedFrameDecoder",
                                new LengthFieldBasedFrameDecoder(CommunicationConstant.MAX_FRAME_LENGTH,
                                        CommunicationConstant.LENGTH_FIELD_OFFSET,
                                        CommunicationConstant.LENGTH_FIELD_LENGTH,
                                        CommunicationConstant.LENGTH_ADJUSTMENT,
                                        CommunicationConstant.INITIAL_BYTES_TO_STRIP))
                        //解码器
                        .addLast("JamesDecoder",JamesDecoder.getInstance(getGlobalConfig().getSerializer()))
                        //业务逻辑+心跳检测处理器,因为业务逻辑比较单一,所以没有分功能写handler,而是内部用if/else区分
                        .addLast("JamesClientHandler",new JamesClientHandler(JamesClient.this));
            }
        };
    }
}
