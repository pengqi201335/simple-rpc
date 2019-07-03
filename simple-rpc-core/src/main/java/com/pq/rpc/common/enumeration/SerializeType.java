package com.pq.rpc.common.enumeration;

import com.pq.rpc.common.enumeration.support.ExtensionBaseType;
import com.pq.rpc.serialize.api.Serializer;
import com.pq.rpc.serialize.hessian.HessianSerializer;
import com.pq.rpc.serialize.jdk.JDKSerializer;
import com.pq.rpc.serialize.json.JsonSerializer;
import com.pq.rpc.serialize.protostuff.ProtostuffSerializer;

public enum SerializeType implements ExtensionBaseType<Serializer> {
    JDK(new JDKSerializer()),                   //jdk原生序列化
    HESSIAN(new HessianSerializer()),           //hessian序列化
    PROTOSTUFF(new ProtostuffSerializer()),     //protoStuff序列化
    JSON(new JsonSerializer());                 //Json序列化

    private Serializer serializer;

    SerializeType(Serializer serializer){
        this.serializer = serializer;
    }

    @Override
    public Serializer getInstance() {
        return serializer;
    }
}
