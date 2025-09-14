package com.kawaiichainwallet.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kawaiichainwallet")
public class MerchantStarter {

    public static void main(String[] args) {
        SpringApplication.run(MerchantStarter.class, args);
    }

}
