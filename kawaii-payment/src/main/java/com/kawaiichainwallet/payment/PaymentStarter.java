package com.kawaiichainwallet.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kawaiichainwallet")
public class PaymentStarter {

    public static void main(String[] args) {
        SpringApplication.run(PaymentStarter.class, args);
    }

}
