package com.pq.rpc;

import com.pq.rpc.client.call.SyncCallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class RPCClientApplication implements CommandLineRunner {

    @Autowired
    private SyncCallService syncCallService;

    public static void main( String[] args )
    {
        SpringApplication app = new SpringApplication(RPCClientApplication.class);
        app.run(args);

    }

    @Override
    public void run(String... args) throws Exception {
        syncCallService.syncCallTest();
    }
}
