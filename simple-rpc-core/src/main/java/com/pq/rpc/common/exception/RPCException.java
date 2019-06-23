package com.pq.rpc.common.exception;

import com.pq.rpc.common.enumeration.ExceptionEnum;

/**
 * 自定义RPC异常
 *
 * @author pengqi
 * create at 2019/6/21
 */
public class RPCException extends RuntimeException{

    /**
     * 异常枚举类实例
     */
    private ExceptionEnum exceptionEnum;

    public RPCException(ExceptionEnum exceptionEnum,String message){
        super(message);
        this.exceptionEnum = exceptionEnum;
    }

    public RPCException(Throwable throwable,ExceptionEnum exceptionEnum,String message){
        super(message,throwable);
        this.exceptionEnum = exceptionEnum;
    }
}
