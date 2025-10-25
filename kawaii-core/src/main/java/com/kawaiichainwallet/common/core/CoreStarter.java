package com.kawaiichainwallet.common.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.kawaiichainwallet")
@EnableFeignClients
public class CoreStarter {

    public static void main(String[] args) {
        SpringApplication.run(CoreStarter.class, args);
    }

}
