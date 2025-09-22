package com.kawaiichainwallet.user.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * 用户上下文持有者
 * 从网关传递的 HTTP Headers 中提取用户信息
 */
@Slf4j
public class UserContextHolder {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_REQUEST_SOURCE = "X-Request-Source";

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String userId = request.getHeader(HEADER_USER_ID);
            log.debug("Retrieved user ID from context: {}", userId);
            return userId;
        }
        return null;
    }

    /**
     * 获取当前用户邮箱
     */
    public static String getCurrentUserEmail() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            return request.getHeader(HEADER_USER_EMAIL);
        }
        return null;
    }

    /**
     * 获取当前用户角色列表
     */
    public static List<String> getCurrentUserRoles() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String roles = request.getHeader(HEADER_USER_ROLES);
            if (roles != null && !roles.trim().isEmpty()) {
                return Arrays.asList(roles.split(","));
            }
        }
        return Collections.emptyList();
    }

    /**
     * 验证请求是否来自网关
     */
    public static boolean isFromGateway() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String source = request.getHeader(HEADER_REQUEST_SOURCE);
            return "gateway".equals(source);
        }
        return false;
    }


    /**
     * 检查当前用户是否具有指定角色
     */
    public static boolean hasRole(String role) {
        List<String> userRoles = getCurrentUserRoles();
        return userRoles.contains(role);
    }

    /**
     * 检查当前用户是否具有任一指定角色
     */
    public static boolean hasAnyRole(String... roles) {
        List<String> userRoles = getCurrentUserRoles();
        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }

    /**
     * 获取完整的用户上下文信息
     */
    public static UserContextInfo getCurrentUserContext() {
        return UserContextInfo.builder()
            .userId(getCurrentUserId())
            .email(getCurrentUserEmail())
            .roles(getCurrentUserRoles())
            .fromGateway(isFromGateway())
            .build();
    }

    /**
     * 获取当前 HTTP 请求
     */
    private static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            log.debug("No HTTP request in current context: {}", e.getMessage());
            return null;
        }
    }
}