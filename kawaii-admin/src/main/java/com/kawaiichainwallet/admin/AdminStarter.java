package com.kawaiichainwallet.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 管理后台服务启动类
 *
 * @author kawaii-server
 */
@SpringBootApplication(scanBasePackages = {
    "com.kawaiichainwallet.admin",
    "com.kawaiichainwallet.common.spring",
    "com.kawaiichainwallet.common.business"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.kawaiichainwallet.api")
public class AdminStarter {

    public static void main(String[] args) {
        SpringApplication.run(AdminStarter.class, args);
    }
}