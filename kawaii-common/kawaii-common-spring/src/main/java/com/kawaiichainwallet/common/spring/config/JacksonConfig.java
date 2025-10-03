package com.kawaiichainwallet.common.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson统一配置类
 * 所有微服务共享的JSON序列化和反序列化规则
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置Jackson ObjectMapper
     * 使用统一的ObjectMapper工厂创建
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return ObjectMapperFactory.createObjectMapper();
    }
}