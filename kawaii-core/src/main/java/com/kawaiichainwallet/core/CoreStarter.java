package com.kawaiichainwallet.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kawaiichainwallet")
public class CoreStarter {

    public static void main(String[] args) {
        SpringApplication.run(CoreStarter.class, args);
    }

}
