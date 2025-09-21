package com.kawaiichainwallet.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Gateway网关服务启动类
 * 职责：启动应用程序
 */
@SpringBootApplication(scanBasePackages = {
    "com.kawaiichainwallet.gateway",
    "com.kawaiichainwallet.common"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.kawaiichainwallet.api"})
public class GatewayStarter {

    public static void main(String[] args) {
        SpringApplication.run(GatewayStarter.class, args);
    }
}
