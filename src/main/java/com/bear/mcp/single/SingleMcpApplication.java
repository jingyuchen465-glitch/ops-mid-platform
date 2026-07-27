package com.bear.mcp.single;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SingleMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SingleMcpApplication.class, args);
    }
}
