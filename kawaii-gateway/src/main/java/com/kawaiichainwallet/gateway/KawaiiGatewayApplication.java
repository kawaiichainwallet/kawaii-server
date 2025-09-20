package com.kawaiichainwallet.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Kawaii Gateway 网关服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.kawaiichainwallet.api.client"})
@ComponentScan(basePackages = {"com.kawaiichainwallet.gateway", "com.kawaiichainwallet.common"})
public class KawaiiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(KawaiiGatewayApplication.class, args);
    }
}