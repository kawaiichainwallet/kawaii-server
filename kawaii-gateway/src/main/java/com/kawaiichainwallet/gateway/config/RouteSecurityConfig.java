package com.kawaiichainwallet.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;

/**
 * 路由安全配置
 * 定义哪些路径需要认证，哪些是公开的
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.security.routes")
public class RouteSecurityConfig {

    /**
     * 路径匹配器 - 支持Ant风格的通配符（*, **, ?）
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 公开路径 - 无需认证
     */
    private List<String> publicPaths;

    /**
     * 受保护路径 - 需要认证
     */
    private List<String> protectedPaths;

    /**
     * 角色路径映射 - 需要特定角色
     * Key: 路径模式, Value: 所需角色列表
     */
    private Map<String, List<String>> rolePaths;

    /**
     * 管理员路径 - 仅管理员可访问
     */
    private List<String> adminPaths;

    /**
     * 内部路径 - 仅服务间调用
     */
    private List<String> internalPaths;

    /**
     * 可选认证路径 - Token无效也允许访问（但如果有效则解析用户信息）
     */
    private List<String> optionalAuthPaths;

    /**
     * 检查路径是否为公开路径
     */
    public boolean isPublicPath(String path) {
        if (publicPaths == null) {
            return false;
        }
        return publicPaths.stream().anyMatch(pattern -> pathMatches(path, pattern));
    }

    /**
     * 检查路径是否为可选认证路径
     */
    public boolean isOptionalAuthPath(String path) {
        if (optionalAuthPaths == null) {
            return false;
        }
        return optionalAuthPaths.stream().anyMatch(pattern -> pathMatches(path, pattern));
    }

    /**
     * 检查路径是否为受保护路径
     */
    public boolean isProtectedPath(String path) {
        if (protectedPaths == null) {
            return false;
        }
        return protectedPaths.stream().anyMatch(pattern -> pathMatches(path, pattern));
    }

    /**
     * 检查路径是否为管理员路径
     */
    public boolean isAdminPath(String path) {
        if (adminPaths == null) {
            return false;
        }
        return adminPaths.stream().anyMatch(pattern -> pathMatches(path, pattern));
    }

    /**
     * 检查路径是否为内部路径
     */
    public boolean isInternalPath(String path) {
        if (internalPaths == null) {
            return false;
        }
        return internalPaths.stream().anyMatch(pattern -> pathMatches(path, pattern));
    }

    /**
     * 获取路径所需的角色列表
     */
    public List<String> getRequiredRoles(String path) {
        if (rolePaths == null) {
            return List.of();
        }

        return rolePaths.entrySet().stream()
                .filter(entry -> pathMatches(path, entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(List.of());
    }

    /**
     * 路径匹配算法 - 使用Spring的AntPathMatcher
     * 支持Ant风格的通配符：
     * - ? 匹配单个字符
     * - * 匹配0个或多个字符（单层路径）
     * - ** 匹配0个或多个目录
     * <p>
     * 示例：
     * - /api/user/123 匹配 /api/user/*
     * - /api/user/profile/edit 匹配 /api/user/**
     * - /api/user/1 匹配 /api/user/?
     */
    private boolean pathMatches(String path, String pattern) {
        return pathMatcher.match(pattern, path);
    }
}