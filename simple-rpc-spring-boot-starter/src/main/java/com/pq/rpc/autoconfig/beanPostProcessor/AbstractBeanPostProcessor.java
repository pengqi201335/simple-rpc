package com.pq.rpc.autoconfig.beanPostProcessor;

import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 抽象的后置处理器,当spring容器初始化完成后,调用后置处理器完成ReferenceConfig和ServiceConfig的创建
 *
 * @author pengqi
 * create at 2019/7/2
 */
public abstract class AbstractBeanPostProcessor implements BeanPostProcessor {

}
