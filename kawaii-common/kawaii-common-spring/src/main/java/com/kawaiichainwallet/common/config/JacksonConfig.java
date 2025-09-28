package com.kawaiichainwallet.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.TimeZone;

/**
 * Jackson统一配置类
 * 所有微服务共享的JSON序列化和反序列化规则
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置Jackson ObjectMapper
     * 统一所有微服务的JSON处理规则
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册Java 8时间模块，支持LocalDateTime等类型
        mapper.registerModule(new JavaTimeModule());

        // 禁用将日期写为时间戳的功能，使用ISO 8601格式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 忽略未知属性，避免反序列化失败，提高API兼容性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 忽略空值属性，减少JSON体积
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 设置时区为UTC，与系统时区策略保持一致
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));

        // 启用缩进输出（仅在开发环境建议使用）
        // mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper;
    }
}