package com.pq.rpc.registry.api;

/**
 * 服务上线或更新回调接口
 * ClusterInvoker调用discover()发现服务时，提供了此接口的实现类作为参数传入
 * 当某个服务器上线或更新了服务时，注册中心回调该实现类的此方法，添加/更新对应的serviceURL
 *
 * @author pengqi
 * create at 2019/6/20
 */
public interface ServiceAddOrUpdateCallback {

    void addOrUpdate(ServiceURL serviceURL);
}
