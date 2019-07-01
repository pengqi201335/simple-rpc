package com.pq.rpc.common.enumeration.support;

import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;

/**
 * 枚举单例接口,用于根据配置信息获取枚举实例
 * @param <T>
 *
 * @author pengqi
 * create at 2019/7/1
 */
public interface ExtensionBaseType<T> {

    T getInstance();

    @SuppressWarnings("unchecked")
    static ExtensionBaseType valueOf(Class enumType,String s){
        Enum wantedEnum = Enum.valueOf(enumType,s);
        if(wantedEnum instanceof ExtensionBaseType){
            return (ExtensionBaseType) wantedEnum;
        }else{
            throw new RPCException(ExceptionEnum.VALUE_OF_MUST_BE_APPLIED_TO_EXTENSION_ENUM_TYPE,"VALUE_OF_MUST_BE_APPLIED_TO_EXTENSION_ENUM_TYPE");
        }
    }
}
