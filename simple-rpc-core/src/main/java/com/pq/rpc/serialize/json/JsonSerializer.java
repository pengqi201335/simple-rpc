package com.pq.rpc.serialize.json;

import com.alibaba.fastjson.JSONObject;
import com.pq.rpc.serialize.api.Serializer;

/**
 * 使用alibaba的Json包,实现基于文档的序列化
 * 可读性好,效率低
 *
 * @author pengqi
 * create at 2019/6/28
 */
public class JsonSerializer implements Serializer {
    /**
     * 序列化
     *
     * @param o 待序列化对象
     * @return 序列化字节数组
     */
    @Override
    public <T> byte[] serialize(T o) {
        return JSONObject.toJSONBytes(o);
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
        return JSONObject.parseObject(bytes,clazz);
    }
}
