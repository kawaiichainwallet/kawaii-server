package com.kawaiichainwallet.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * 请求日志配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "gateway.request-logging")
public class RequestLoggingConfig {

    /**
     * 是否启用请求日志
     */
    private boolean enabled = true;

    /**
     * 是否记录请求头
     */
    private boolean logHeaders = true;

    /**
     * 是否记录请求体
     */
    private boolean logRequestBody = false;

    /**
     * 是否记录响应体
     */
    private boolean logResponseBody = false;

    /**
     * 记录响应体的最大大小（字节）
     */
    private int maxResponseBodySize = 1024;

    /**
     * 是否记录敏感路径的详细信息
     */
    private boolean logSensitivePaths = false;

    /**
     * 排除的路径模式
     */
    private Set<String> excludePaths = Set.of(
        "/actuator/**",
        "/health",
        "/favicon.ico"
    );

    /**
     * 需要脱敏的字段名
     */
    private Set<String> sensitiveFields = Set.of(
        "password", "token", "authorization", "cookie", "session",
        "secret", "key", "passwd", "pwd", "credential", "refreshToken",
        "accessToken", "otp", "otpCode", "verificationCode"
    );

    /**
     * 敏感路径模式
     */
    private Set<String> sensitivePaths = Set.of(
        "/auth/login",
        "/auth/register",
        "/auth/refresh",
        "/user/password",
        "/admin/login",
        "/otp/**"
    );

    /**
     * 强制记录的路径
     */
    private Set<String> forceLogPaths = Set.of(
        // 暂时为空
    );
}