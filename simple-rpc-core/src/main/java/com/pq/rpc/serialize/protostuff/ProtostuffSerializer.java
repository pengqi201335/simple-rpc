package com.pq.rpc.serialize.protostuff;

import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.serialize.api.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * 利用google的protostuff包进行序列化和反序列化
 * 效率高,可读性差,适用于RPC请求数据传输
 *
 * @author pengqi
 * create at 2019/6/29
 */
public class ProtostuffSerializer implements Serializer {
    /**
     * 序列化
     *
     * @param o 待序列化对象
     * @return 序列化字节数组
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> byte[] serialize(T o) throws RPCException {
        Class clz = o.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try{
            Schema schema = RuntimeSchema.createFrom(clz);
            return ProtostuffIOUtil.toByteArray(o,schema,buffer);
        }catch (Exception e){
            e.printStackTrace();
            throw new RPCException(ExceptionEnum.SERIALIZE_ERROR,"SERIALIZE_ERROR");
        }finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化
     *
     * @param bytes 字节数组
     * @param clazz 目标对象类类型
     * @return 目标对象
     */
    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) throws RPCException {
        T o = objenesis.newInstance(clazz); //实例化一个对象
        Schema<T> schema = RuntimeSchema.createFrom(clazz);
        ProtostuffIOUtil.mergeFrom(bytes,o,schema); //给对象赋值
        return o;
    }

    //实例化序列化后的对象
    private Objenesis objenesis = new ObjenesisStd();
}
