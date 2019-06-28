package com.pq.rpc.common.domain;

import com.pq.rpc.common.util.TypeUtil;
import io.netty.util.Recycler;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

/**
 * RPC调用请求对象
 * 进行RPC调用时要被序列化,所以实现了Serializable接口
 *
 * @author pengqi
 * create at 2019/6/23
 */
@Data
public class RPCRequest implements Serializable {

    private String requestID;

    private String interfaceName;

    private String methodName;

    private String[] parameterTypes;

    private Object[] parameters;

    /**
     * handle为Recycler对象池中封装RPCRequest的对象,不可序列化
     * 任何使用Recycler对象池复用技术的对象都会被封装成DefaultHandle对象(实现了Handle接口)
     * 此接口只有一个方法:recycle(),用于回收对象至对象池
     */
    private final transient Recycler.Handle<RPCRequest> handle;

    public RPCRequest(Recycler.Handle<RPCRequest> handle){
        this.handle = handle;
    }

    public Class[] getParameterTypes(){
        Class[] parameterTypeClasses = new Class[parameterTypes.length];
        for(int i=0;i<parameterTypes.length;i++){
            String type = parameterTypes[i];
            try{
                if(TypeUtil.isPrimitive(type)){
                    //基本类型
                    parameterTypeClasses[i] = TypeUtil.map(type);
                }else {
                    //非基本类型
                    parameterTypeClasses[i] = Class.forName(type);
                }
            }catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
        return parameterTypeClasses;
    }

    public void setParameterTypes(String[] parameterTypes){
        this.parameterTypes = parameterTypes;
    }

    public void setParameterTypes(Class[] parameterTypes){
        String[] paramTypes = new String[parameterTypes.length];
        for(int i=0;i<parameterTypes.length;i++){
            paramTypes[i] = parameterTypes[i].getName();
        }
        this.parameterTypes = paramTypes;
    }

    public String key(){
        return interfaceName+
                "."+
                methodName+
                "."+
                Arrays.toString(parameterTypes)+
                "."+
                Arrays.toString(parameters);
    }

    public void recycle(){
        requestID = null;
        interfaceName = null;
        methodName = null;
        parameterTypes = null;
        parameters = null;
        handle.recycle(this);
    }
}
