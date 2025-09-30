package com.kawaiichainwallet.common.spring.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Feign客户端通用配置
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Feign日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * 请求拦截器 - 添加内部服务间标识和用户上下文传递
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new InternalServiceRequestInterceptor();
    }

    /**
     * 内部服务请求拦截器 - 传递服务标识和用户上下文
     */
    public static class InternalServiceRequestInterceptor implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate template) {
            // 1. 添加内部服务标识
            template.header("X-Service-Source", "internal");
            template.header("X-Request-Source", "feign-client");

            // 2. 传递用户上下文（如果存在）
            try {
                ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();

                    // 传递用户ID
                    String userId = request.getHeader("X-User-Id");
                    if (userId != null) {
                        template.header("X-User-Id", userId);
                    }

                    // 传递用户角色
                    String userRoles = request.getHeader("X-User-Roles");
                    if (userRoles != null) {
                        template.header("X-User-Roles", userRoles);
                    }

                    // 传递请求追踪ID
                    String traceId = request.getHeader("X-Trace-Id");
                    if (traceId != null) {
                        template.header("X-Trace-Id", traceId);
                    }
                }
            } catch (Exception e) {
                // 静默处理获取请求上下文失败的情况
                // 在微服务内部调用时，这种情况是正常的
            }
        }
    }
}