package com.pq.rpc;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class RPCServerApplication implements CommandLineRunner {
    public static void main( String[] args )
    {
        SpringApplication app = new SpringApplication(RPCServerApplication.class);

    }

    @Override
    public void run(String... args) throws Exception {

    }
}
