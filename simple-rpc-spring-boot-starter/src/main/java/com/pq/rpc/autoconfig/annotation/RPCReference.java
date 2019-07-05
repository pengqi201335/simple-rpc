package com.pq.rpc.autoconfig.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务引用注解类
 * 添加@RPCReference的字段将被注入一个对应服务的实现类的代理bean
 *
 * @author pengqi
 * create at 2019/7/2
 */
@Target(ElementType.FIELD)  //表示该注解是用在字段上的
@Retention(RetentionPolicy.RUNTIME)
public @interface RPCReference {
    //是否异步,默认为false
    boolean async() default false;
    //...
    boolean callback() default false;

    boolean oneWay() default false;

    //超时时间
    long timeout() default 3000;

    //回调方法
    String callbackMethod() default "";

    int callbackParamIndex() default 1;
}
