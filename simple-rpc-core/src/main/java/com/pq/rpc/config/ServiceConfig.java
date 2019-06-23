package com.pq.rpc.config;

import com.pq.rpc.config.support.AbstractConfig;
import com.pq.rpc.protocol.api.Exporter;
import com.pq.rpc.protocol.api.Invoker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务配置类，主要配置以下属性:
 * 1)服务接口名
 * 2)服务接口类型
 * 3)是否有回调参数
 * 4)回调方法
 * 5)回调参数索引
 * 6)是否回调接口
 * 7)服务的本地代理
 * 8)服务暴露之后的抽象调用者
 *
 * @author pengqi
 * create at 2019/6/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceConfig<T> extends AbstractConfig {

    private String interfaceName;

    private Class<T> interfaceClass;

    private boolean isCallback;

    private String callbackMethod;

    private int callbackParamIndex;

    private boolean isCallbackInterface;

    private T ref;

    private Exporter<T> exporter;

    /**
     * 暴露服务
     */
    public void export(){
        //根据该服务的本地代理及其接口类型创建一个抽象调用者
        Invoker<T> invoker = getApplicationConfig().getRPCProxyFactoryInstance().getInvoker(ref,interfaceClass);
        //将上述抽象调用者和ServiceConfig自身封装起来,进行远程服务暴露
        exporter = getProtocolConfig().getProtocolInstance().export(invoker,this);
    }
}
