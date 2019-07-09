package com.pq.rpc.client.call;

import com.pq.rpc.api.domain.User;
import com.pq.rpc.api.server.SimpleRPCService;
import com.pq.rpc.autoconfig.annotation.RPCReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 同步调用测试
 *
 * @author pengqi
 * create at 2019/7/5
 */
@Component
@Slf4j
public class SyncCallService {

    private SimpleRPCService rpcService;    //远程服务的本地代理

    public void syncCallTest(){
        String result = rpcService.helloRPC(new User("pengqidalao"));
        log.info("同步调用结果:{}",result);
    }
}
