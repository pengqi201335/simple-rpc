package com.pq.rpc.protocol.api.support;

import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.config.GlobalConfig;
import com.pq.rpc.config.ServiceConfig;
import com.pq.rpc.protocol.api.Exporter;
import com.pq.rpc.protocol.api.Protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象的Protocol实例,实现了protocol的一些通用功能,供具体协议对象继承使用
 * 模板方法模式
 * 抽象的protocol实现了引用本地服务的方法
 * 引用远程服务的方法放在具体的xxxProtocol中实现
 *
 * @author pengqi
 * create at 2019/6/22
 */
public abstract class AbstractProtocol implements Protocol {
    //存储已暴露的抽象服务调用者
    private Map<String, Exporter<?>> exportedMap = new ConcurrentHashMap<>();

    private GlobalConfig globalConfig;

    //将用户配置传入Protocol层
    public void init(GlobalConfig globalConfig){
        this.globalConfig = globalConfig;
    }

    protected GlobalConfig getGlobalConfig(){
        return globalConfig;
    }

    /**
     * 将暴露的服务放入map中存储
     * @param interfaceClass 暴露的服务接口
     * @param exporter 服务暴露之后的抽象调用者
     */
    public void putExporter(Class<?> interfaceClass,Exporter<?> exporter){
        exportedMap.put(interfaceClass.getName(),exporter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ServiceConfig<T> referLocalService(String interfaceName) throws RPCException {
        if(!exportedMap.containsKey(interfaceName)){
            //该服务还未进行远程暴露
            throw new RPCException(ExceptionEnum.SERVER_NOT_EXPORTED,"THIS SERVER IS NOT EXPORTED YET");
        }
        //从已暴露的服务列表中拿出对应的exporter，并取出其内部的serviceConfig
        return (ServiceConfig<T>) exportedMap.get(interfaceName).getServiceConfig();
    }
}
