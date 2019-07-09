package com.pq.rpc.transport.James.codec;

import com.pq.rpc.common.domain.Message;
import com.pq.rpc.serialize.api.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 面向James协议的编码器
 *
 * @author pengqi
 * create at 2019/6/26
 */
@Slf4j
public class JamesEncoder extends MessageToByteEncoder<Message> {

    /**
     * 单例模式,多个pipeline共享handler
     */
//    private static volatile JamesEncoder INSTANCE = null;

    private Serializer serializer;  //序列化算法

    public JamesEncoder(Serializer serializer){
        this.serializer = serializer;
    }

    //双重检测锁实现单例的延迟加载
//    public static JamesEncoder getInstance(Serializer serializer){
//        if(INSTANCE==null){
//            synchronized (JamesEncoder.class){
//                if(INSTANCE==null){
//                    synchronized (JamesEncoder.class){
//                        INSTANCE = new JamesEncoder(serializer);
//                    }
//                }
//            }
//        }
//        return INSTANCE;
//    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf byteBuf) {
        //先写消息类型
        byteBuf.writeByte(msg.getType());
        if(msg.getType()==Message.REQUEST){
            //消息为请求对象
            //序列化请求对象
            byte[] bytes = serializer.serialize(msg.getRequest());
            byteBuf.writeBytes(bytes);
            //将msg中的request对象回收
            //msg.getRequest().recycle();
        }else if(msg.getType()==Message.RESPONSE){
            //消息为响应对象
            //序列化响应对象
            byte[] bytes = serializer.serialize(msg.getResponse());
            byteBuf.writeBytes(bytes);
            //将msg中的response对象回收
            msg.getResponse().recycle();
        }
        log.info("完成编码！！！");
    }
}
