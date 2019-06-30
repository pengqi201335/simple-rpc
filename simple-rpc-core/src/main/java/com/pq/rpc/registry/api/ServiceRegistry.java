package com.pq.rpc.registry.api;

/**
 * 服务注册中心接口
 * 定义了服务注册、服务发现、初始化注册中心和关闭注册中心等方法
 *
 * @author pengqi
 * create at 2019/6/20
 */
public interface ServiceRegistry {

    void init();

    /**
     * 服务发现方法,用于consumer
     * @param interfaceName 服务接口名
     * @param serviceOfflineCallback 服务下线回调函数
     * @param serviceAddOrUpdateCallback 服务上线/更新回调函数
     */
    void discover(String interfaceName,ServiceOfflineCallback serviceOfflineCallback,ServiceAddOrUpdateCallback serviceAddOrUpdateCallback);

    /**
     * 服务注册方法,用于provider
     * @param interfaceName 服务名
     * @param serviceAddress　服务提供方地址
     * @param interfaceClass 服务接口Class对象
     */
    void register(String serviceAddress,String interfaceName,Class<?> interfaceClass);

    void close();
}
