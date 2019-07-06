package com.pq.rpc.autoconfig.beanPostProcessor;

import com.pq.rpc.config.*;
import com.pq.rpc.config.support.AbstractConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 抽象的后置处理器,当spring容器初始化完成后,调用后置处理器完成ReferenceConfig和ServiceConfig的创建
 *
 * @author pengqi
 * create at 2019/7/2
 */
public abstract class AbstractBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private GlobalConfig globalConfig;

    protected ApplicationContext ac;    //spring容器

    //根据autoConfiguration生成的配置类,初始化全局配置类
    public void init(ApplicationConfig applicationConfig, RegistryConfig registryConfig, ProtocolConfig protocolConfig, ClusterConfig clusterConfig){
        globalConfig = GlobalConfig.builder().
                applicationConfig(applicationConfig).
                registryConfig(registryConfig).
                protocolConfig(protocolConfig).
                clusterConfig(clusterConfig).
                build();
    }

    //将初始化后的全局配置类配置到特定配置类中
    protected void initConfig(AbstractConfig config){
        config.init(globalConfig);
    }

    public static void initConfig(ApplicationContext ctx,AbstractConfig config){
        config.init(
                GlobalConfig.builder()
                .applicationConfig(ctx.getBean(ApplicationConfig.class))
                .protocolConfig(ctx.getBean(ProtocolConfig.class))
                .registryConfig(ctx.getBean(RegistryConfig.class))
                .clusterConfig(ctx.getBean(ClusterConfig.class))
                .build()
        );
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ac = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
