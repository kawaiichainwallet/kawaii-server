package com.kawaiichainwallet.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
     * 检查路径是否为公开路径
     */
    public boolean isPublicPath(String path) {
        if (publicPaths == null) {
            return false;
        }
        return publicPaths.stream().anyMatch(pattern -> pathMatches(path, pattern));
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
     * 路径匹配算法
     * 支持通配符 * 和 **
     */
    private boolean pathMatches(String path, String pattern) {
        // 简单实现：支持 ** 通配符
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }

        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            String remaining = path.substring(prefix.length());
            return path.startsWith(prefix) && !remaining.contains("/");
        }

        return pattern.equals(path);
    }
}