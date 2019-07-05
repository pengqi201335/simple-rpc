package com.pq.rpc.server.service;

import com.pq.rpc.api.callback.CallbackInterface;
import com.pq.rpc.api.domain.User;
import com.pq.rpc.api.server.RPCServiceWithCallback;
import com.pq.rpc.autoconfig.annotation.RPCService;

/**
 * 带回调参数的服务接口实现类
 *
 * @author pengqi
 * create at 2019/7/5
 */
@RPCService
public class RPCServiceWithCallbackImpl implements RPCServiceWithCallback {
    @Override
    public void hello(User user, CallbackInterface callbackInterface) {
        String result = "i am RPCServer,i'm calling "+user.getUserName()+" back...";
        callbackInterface.getInfoFromClient(result);
    }
}
