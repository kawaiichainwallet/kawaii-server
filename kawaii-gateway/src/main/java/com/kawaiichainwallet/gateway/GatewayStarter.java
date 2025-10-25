package com.kawaiichainwallet.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

/**
 * Gateway网关服务启动类
 * 职责：启动应用程序
 *
 * <p><b>时区设置</b>：
 * 应用启动时强制设置 JVM 默认时区为 UTC，
 * 确保所有时间操作统一使用 UTC 时区，避免时区相关的 bug。
 * </p>
 */
@SpringBootApplication(scanBasePackages = "com.kawaiichainwallet")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.kawaiichainwallet.api")
public class GatewayStarter {

    static {
        // 强制设置 JVM 默认时区为 UTC
        // 防止因服务器时区设置不同导致的时间问题
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.println("JVM 默认时区已设置为: " + TimeZone.getDefault().getID());
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayStarter.class, args);
    }
}
