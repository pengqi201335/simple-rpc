package com.pq.rpc.server.service;

import com.pq.rpc.api.domain.User;
import com.pq.rpc.api.server.SimpleRPCService;
import com.pq.rpc.autoconfig.annotation.RPCService;
import org.springframework.stereotype.Component;

/**
 * 简单RPC接口的实现类
 *
 * @author pengqi
 * create at 2019/7/5
 */
@RPCService(interfaceClass = SimpleRPCService.class)
public class SimpleRPCServiceImpl implements SimpleRPCService {
    @Override
    public String helloRPC(User user) {

        return "remote_call success!";
    }
}
