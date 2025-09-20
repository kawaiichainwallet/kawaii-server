package com.kawaiichainwallet.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Kawaii User 用户服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.kawaiichainwallet.api.client"})
@ComponentScan(basePackages = {"com.kawaiichainwallet.user", "com.kawaiichainwallet.common"})
public class KawaiiUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(KawaiiUserApplication.class, args);
    }
}