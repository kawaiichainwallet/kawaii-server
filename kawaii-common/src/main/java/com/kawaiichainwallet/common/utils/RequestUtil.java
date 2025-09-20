package com.kawaiichainwallet.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP请求工具类
 */
@Slf4j
public class RequestUtil {

    private static final String UNKNOWN = "unknown";
    private static final String IP_SEPARATOR = ",";
    private static final int IP_MAX_LENGTH = 15;

    /**
     * 获取客户端真实IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // 多级代理时，第一个IP为客户端真实IP
            int index = ip.indexOf(IP_SEPARATOR);
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return ip.trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }

        // 如果以上都没有获取到，则使用request.getRemoteAddr()
        ip = request.getRemoteAddr();
        return ip == null ? UNKNOWN : ip;
    }

    /**
     * 获取用户代理字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String userAgent = request.getHeader("User-Agent");
        return userAgent == null ? UNKNOWN : userAgent;
    }

    /**
     * 获取当前用户ID（从请求头或Security Context中获取）
     */
    public static String getCurrentUserId() {
        // 这里应该从Spring Security Context或JWT Token中获取用户ID
        // 暂时返回null，具体实现需要集成JWT解析逻辑
        return null;
    }

    /**
     * 验证IP地址是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }
}