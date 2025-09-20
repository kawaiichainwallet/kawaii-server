package com.kawaiichainwallet.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {
        "com.kawaiichainwallet.payment",
        "com.kawaiichainwallet.common"
})
public class PaymentStarter {

    public static void main(String[] args) {
        SpringApplication.run(PaymentStarter.class, args);
    }

}
