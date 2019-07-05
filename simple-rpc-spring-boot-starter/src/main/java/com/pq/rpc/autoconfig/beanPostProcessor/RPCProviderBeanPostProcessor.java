package com.pq.rpc.autoconfig.beanPostProcessor;

import com.pq.rpc.autoconfig.annotation.RPCService;
import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;

/**
 * RPC服务端后置处理器,用于创建ServiceConfig实例
 *
 * @author pengqi
 * create at 2019/7/4
 */
@Slf4j
public class RPCProviderBeanPostProcessor extends AbstractBeanPostProcessor{
    @Override
    @SuppressWarnings("unchecked")
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if(!beanClass.isAnnotationPresent(RPCService.class)){
            //此类未添加@RPCService注解
            return bean;
        }

        RPCService service = beanClass.getAnnotation(RPCService.class);
        Class<?> interfaceClass = service.interfaceClass();
        if(interfaceClass == void.class){
            Class<?>[] interfaces = beanClass.getInterfaces();
            if(interfaces.length>=1){
                interfaceClass = interfaces[0];
            }else{
                throw new RPCException(ExceptionEnum.SERVICE_DID_NOT_IMPLEMENT_ANY_INTERFACE,beanName+"未实现任何服务接口");
            }
        }
        ServiceConfig serviceConfig = ServiceConfig.builder().
                interfaceName(interfaceClass.getName()).
                interfaceClass((Class<Object>)interfaceClass).
                isCallback(service.callback()).
                callbackMethod(service.callbackMethod()).
                callbackParamIndex(service.callbackParamIndex()).
                ref(bean).
                build();
        initConfig(serviceConfig);
        serviceConfig.export();     //暴露此服务
        log.info("暴露服务:{}",interfaceClass);
        return bean;
    }
}
