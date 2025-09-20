package com.kawaiichainwallet.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.kawaiichainwallet")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.kawaiichainwallet.api.client"})
@MapperScan("com.kawaiichainwallet.user.mapper")
public class UserStarter {

    public static void main(String[] args) {
        SpringApplication.run(UserStarter.class, args);
    }
}