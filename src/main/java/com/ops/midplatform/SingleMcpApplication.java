package com.ops.midplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@MapperScan("com.ops.midplatform.core.mapper")
@SpringBootApplication
public class SingleMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SingleMcpApplication.class, args);
    }
}
