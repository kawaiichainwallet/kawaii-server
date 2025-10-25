package com.kawaiichainwallet.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.kawaiichainwallet")
@EnableFeignClients
public class PaymentStarter {

    public static void main(String[] args) {
        SpringApplication.run(PaymentStarter.class, args);
    }

}
