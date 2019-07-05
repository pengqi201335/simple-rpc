package com.pq.rpc.api.server;

import com.pq.rpc.api.callback.CallbackInterface;
import com.pq.rpc.api.domain.User;

/**
 * 带回调参数的服务接口
 *
 * @author pengqi
 * create at 2019/7/5
 */
public interface RPCServiceWithCallback {
    void hello(User user, CallbackInterface callbackInterface);
}
