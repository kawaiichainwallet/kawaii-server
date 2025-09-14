package com.kawaiichainwallet.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kawaiichainwallet")
public class UserStarter {

    public static void main(String[] args) {
        SpringApplication.run(UserStarter.class, args);
    }

}