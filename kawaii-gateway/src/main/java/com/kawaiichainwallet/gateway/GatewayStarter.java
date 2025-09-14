package com.kawaiichainwallet.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kawaiichainwallet")
public class GatewayStarter {

    public static void main(String[] args) {
        SpringApplication.run(GatewayStarter.class, args);
    }

}
