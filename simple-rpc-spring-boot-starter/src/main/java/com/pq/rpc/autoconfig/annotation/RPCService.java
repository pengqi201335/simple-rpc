package com.pq.rpc.autoconfig.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 远程服务的实现注解类
 * 添加了@RPCService注解的类将被暴露到注册中心
 *
 * @author pengqi
 * create at 2019/7/2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RPCService {
    //实现的接口
    Class<?> interfaceClass() default void.class;

    boolean callback() default false;

    String callbackMethod() default "";

    int callbackParamIndex() default 1;
}
