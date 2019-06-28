package com.pq.rpc.common.domain;

import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import io.netty.util.Recycler;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于Netty的全局对象池(Recycler)
 * 实现对象的复用,避免频繁的创建对象以及GC
 *
 * @author pengqi
 * create at 2019/6/27
 */
public class GlobalRecycler {

    /**
     * 存储对象类型及其对象池
     */
    private static Map<Class<?>, Recycler<?>> RECYCLER = new HashMap<>();

    static {
        RECYCLER.put(RPCRequest.class, new Recycler<RPCRequest>() {
            @Override
            protected RPCRequest newObject(Handle<RPCRequest> handle) {
                return new RPCRequest(handle);
            }
        });
        RECYCLER.put(RPCResponse.class, new Recycler<RPCResponse>() {
            @Override
            protected RPCResponse newObject(Handle<RPCResponse> handle) {
                return new RPCResponse(handle);
            }
        });
    }

    public static boolean isReusable(Class<?> clazz){
        return RECYCLER.containsKey(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T reuse(Class<T> clazz){
        if(!isReusable(clazz)){
            throw new RPCException(ExceptionEnum.RECYCLER_ERROR,"RECYCLER_ERROR");
        }
        return (T)RECYCLER.get(clazz).get();
    }
}
