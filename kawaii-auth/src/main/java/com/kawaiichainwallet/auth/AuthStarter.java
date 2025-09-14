package com.kawaiichainwallet.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kawaiichainwallet")
public class AuthStarter {

    public static void main(String[] args) {
        SpringApplication.run(AuthStarter.class, args);
    }

}
