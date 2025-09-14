package com.kawaiichainwallet.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kawaiichainwallet")
public class NotificationStarter {

    public static void main(String[] args) {
        SpringApplication.run(NotificationStarter.class, args);
    }

}
