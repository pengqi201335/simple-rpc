package com.pq.rpc.serialize.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.HessianOutput;
import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.serialize.api.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * hessian协议序列化/反序列化算法
 * 基于原生JDK
 *
 * @author pengqi
 * create at 2019/6/29
 */
public class HessianSerializer implements Serializer {
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
            HessianOutput output = new HessianOutput(baos);
            output.writeObject(o);
            output.flush();
            return baos.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
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
    @SuppressWarnings("unchecked")
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) throws RPCException {
        try{
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            Hessian2Input input = new Hessian2Input(bais);
            return (T)input.readObject(clazz);
        }catch (IOException e){
            e.printStackTrace();
            throw new RPCException(ExceptionEnum.DESERIALIZE_ERROR,"DESERIALIZE_ERROR");
        }
    }
}
