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
    FAIL_TO_GET_LOCALHOST_ADDRESS("获取本地主机地址失败"),
    FAIL_TO_CONNECT_TO_SERVER("连接服务器失败"),
    SUBMIT_AFTER_ENDPOINT_CLOSE("服务器已关闭连接,此时不可再提交请求"),
    HEART_BEAT_TIMES_EXCEED("超过心跳重试次数"),
    RECYCLER_ERROR("对象不可复用"),
    TRANSPORT_FAILURE("通信层异常"),
    SERIALIZE_ERROR("序列化异常"),
    DESERIALIZE_ERROR("反序列化异常"),
    REGISTRY_ERROR("ZK异常"),
    NO_AVAILABLE_SERVICE("无可用服务"),
    EXCEEDED_RETRIES("超出重试次数"),
    VALUE_OF_MUST_BE_APPLIED_TO_EXTENSION_ENUM_TYPE("调用valueOf的枚举类必须实现ExtensionEnumType接口"),
    EXTENSION_CONFIG_ERROR("扩展点配置出现错误"),
    NO_SUPPORTED_INSTANCE("无可用服务"),
    SIMPLE_RPC_CONFIG_ERROR("simple-rpc依赖配置发生错误"),
    SERVICE_DID_NOT_IMPLEMENT_ANY_INTERFACE("该类未实现任何服务接口");


    private String errorMessage;

    ExceptionEnum(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage(){
        return errorMessage;
    }
}
