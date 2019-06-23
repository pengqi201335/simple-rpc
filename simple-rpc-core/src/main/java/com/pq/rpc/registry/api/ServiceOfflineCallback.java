package com.pq.rpc.registry.api;

import java.util.List;

/**
 * 服务下线回调接口
 * ClusterInvoker调用discover()发现服务时，提供了此接口的实现类作为参数传入
 * 当某个服务器下线了服务时，注册中心回调该实现类的此方法，删除对应的ServiceURL
 *
 * @author pengqi
 * create at 2019/6/20
 */
public interface ServiceOfflineCallback {

    void removeNotExisted(List<ServiceURL> newServiceURLS);
}
