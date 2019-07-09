package com.pq.rpc.client.call;

import com.pq.rpc.api.domain.User;
import com.pq.rpc.api.server.SimpleRPCService;
import com.pq.rpc.autoconfig.annotation.RPCReference;
import com.pq.rpc.common.context.RPCThreadLocalContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * 异步调用测试
 *
 * @author pengqi
 * create at 2019/7/5
 */
@Component
@Slf4j
public class AsyncCallService {
    @RPCReference(async = true)
    private SimpleRPCService service;

    public void asyncCall() throws Exception{
        service.helloRPC(new User("zll"));
        Future<String> future = RPCThreadLocalContext.getContext().getFuture();
        log.info("异步调用结果:{}",future.get());
    }
}
