package com.kawaiichainwallet.common.spring.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.TimeZone;

/**
 * ObjectMapper工厂类
 * 提供统一配置的ObjectMapper实例
 */
public class ObjectMapperFactory {

    /**
     * 创建统一配置的ObjectMapper实例
     */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册Java 8时间模块
        mapper.registerModule(new JavaTimeModule());

        // 禁用将日期写为时间戳的功能
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 忽略未知属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 忽略空值属性
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 设置时区为UTC
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));

        return mapper;
    }

    private ObjectMapperFactory() {
        // 工具类不允许实例化
    }
}
