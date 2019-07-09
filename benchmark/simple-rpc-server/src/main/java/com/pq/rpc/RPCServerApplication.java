package com.pq.rpc;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class RPCServerApplication implements CommandLineRunner {
    public static void main( String[] args ) throws InterruptedException{
        SpringApplication app = new SpringApplication(RPCServerApplication.class);
        app.run(args);
        new CountDownLatch(1).await();
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
