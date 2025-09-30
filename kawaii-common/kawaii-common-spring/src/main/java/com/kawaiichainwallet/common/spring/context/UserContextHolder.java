package com.kawaiichainwallet.common.spring.context;

import com.kawaiichainwallet.common.core.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 用户上下文持有者
 * 从HTTP请求头中获取Gateway传递的用户信息
 */
public class UserContextHolder {

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader("X-User-Id") : null;
    }

    /**
     * 获取当前用户邮箱
     */
    public static String getCurrentUserEmail() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader("X-User-Email") : null;
    }

    /**
     * 获取当前用户角色列表
     */
    public static List<String> getCurrentUserRoles() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return Collections.emptyList();
        }

        String rolesHeader = request.getHeader("X-User-Roles");
        if (rolesHeader == null || rolesHeader.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.asList(rolesHeader.split(","));
    }

    /**
     * 检查当前用户是否已认证
     */
    public static boolean isAuthenticated() {
        HttpServletRequest request = getCurrentRequest();
        return request != null && "true".equals(request.getHeader("X-Authenticated"));
    }

    /**
     * 获取完整的用户上下文
     */
    public static UserContext getCurrentUserContext() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        UserContext context = new UserContext();
        context.setUserId(request.getHeader("X-User-Id"));
        context.setEmail(request.getHeader("X-User-Email"));
        context.setRoles(getCurrentUserRoles());
        context.setAuthenticated("true".equals(request.getHeader("X-Authenticated")));
        context.setRequestSource(request.getHeader("X-Request-Source"));

        String timestampHeader = request.getHeader("X-Request-Timestamp");
        if (timestampHeader != null && !timestampHeader.isEmpty()) {
            try {
                context.setRequestTimestamp(Long.parseLong(timestampHeader));
            } catch (NumberFormatException e) {
                // 忽略时间戳解析错误
            }
        }

        return context;
    }

    /**
     * 检查当前用户是否具有指定角色
     */
    public static boolean hasRole(String role) {
        List<String> roles = getCurrentUserRoles();
        return roles.contains(role);
    }

    /**
     * 检查当前用户是否具有任一指定角色
     */
    public static boolean hasAnyRole(String... roles) {
        List<String> userRoles = getCurrentUserRoles();
        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }

    /**
     * 检查当前用户是否为管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 获取当前HTTP请求
     */
    private static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            // 在非Web上下文中调用时会抛出异常，如Swagger文档生成时
            return null;
        }
    }
}