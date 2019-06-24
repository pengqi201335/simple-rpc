package com.pq.rpc.common.enumeration;

/**
 * 异常枚举类
 * 维护一些常见的异常
 *
 * @author pengqi
 * create at 2019/6/21
 */
public enum ExceptionEnum {
    SAME_SERVICE_CONNOT_BE_REFERRED_BY_DIFFERENT_CONFIG("同一个服务在同一个客户端只能以相同的配置被引用"),
    NO_SERVER_FOUND("未找到可用的服务"),
    REMOTE_SERVICE_INVOCATION_FAILED("远程服务调用失败"),
    SERVER_NOT_EXPORTED("此服务还未进行暴露"),
    SERVER_ADDRESS_IS_NOT_CONFIGURATION("当前端点还未配置"),
    GET_PROCESSOR_METHOD_MUST_BE_OVERRIDE("getProcessor()方法未被重写,无法提交RPC请求"),
    FAIL_TO_GET_LOCALHOST_ADDRESS("获取本地主机地址失败");

    private String errorMessage;

    ExceptionEnum(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage(){
        return errorMessage;
    }
}
