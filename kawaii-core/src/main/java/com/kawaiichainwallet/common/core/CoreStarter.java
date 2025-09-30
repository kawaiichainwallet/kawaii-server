package com.kawaiichainwallet.common.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {
        "com.kawaiichainwallet.core",
        "com.kawaiichainwallet.common"
})
public class CoreStarter {

    public static void main(String[] args) {
        SpringApplication.run(CoreStarter.class, args);
    }

}
