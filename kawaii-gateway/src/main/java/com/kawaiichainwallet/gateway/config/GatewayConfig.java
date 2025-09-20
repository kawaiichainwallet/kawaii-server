package com.kawaiichainwallet.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Gateway网关配置类
 */
@Configuration
public class GatewayConfig {

    /**
     * IP限流KeyResolver
     * 基于客户端IP地址进行限流
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                   .getAddress()
                   .getHostAddress()
        );
    }
}