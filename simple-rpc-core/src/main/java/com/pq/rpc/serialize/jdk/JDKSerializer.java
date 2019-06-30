package com.pq.rpc.serialize.jdk;

import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.serialize.api.Serializer;

import java.io.*;

/**
 * 基于JDK自带的序列化实现
 *
 * @author pengqi
 * create at 2019/6/28
 */
public class JDKSerializer implements Serializer {
    /**
     * 序列化
     *
     * @param o 待序列化对象
     * @return 序列化字节数组
     */
    @Override
    public <T> byte[] serialize(T o) throws RPCException {
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            byte[] bytes = baos.toByteArray();
            baos.close();
            oos.close();
            return bytes;
        }catch (Throwable t){
            t.printStackTrace();
            throw new RPCException(ExceptionEnum.SERIALIZE_ERROR,"SERIALIZE_ERROR");
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
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) {
        try{
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object o = ois.readObject();
            return clazz.cast(o);   //强制类型转换
        }catch (Throwable t){
            t.printStackTrace();
            throw new RPCException(ExceptionEnum.DESERIALIZE_ERROR,"DESERIALIZE_ERROR");
        }
    }
}
