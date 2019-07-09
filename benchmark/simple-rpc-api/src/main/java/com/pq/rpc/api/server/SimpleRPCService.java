package com.pq.rpc.api.server;

import com.pq.rpc.api.domain.User;

/**
 * 简单的RPC服务接口
 *
 * @author pengqi
 * create at 2019/7/5
 */
public interface SimpleRPCService {
    String helloRPC(User user);
}
