package com.ai.server.agent.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableDiscoveryClient
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties
@EnableFeignClients(basePackages = {"com.sgcc.c2000"})
@ComponentScan(basePackages = {"com.sgcc.c2000"})
public class MetricAiMain {

    public static void main(String[] args) {
        SpringApplication.run(MetricAiMain.class, args);
    }
}

