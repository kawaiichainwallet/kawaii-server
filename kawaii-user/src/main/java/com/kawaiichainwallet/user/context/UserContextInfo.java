package com.kawaiichainwallet.user.context;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户上下文信息
 */
@Data
@Builder
public class UserContextInfo {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 是否来自网关
     */
    private boolean fromGateway;

    /**
     * 内部token是否有效
     */
    private boolean validInternalToken;

    /**
     * 检查用户是否已认证
     */
    public boolean isAuthenticated() {
        return userId != null && !userId.trim().isEmpty() && fromGateway && validInternalToken;
    }

    /**
     * 检查是否具有指定角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * 检查是否具有任一指定角色
     */
    public boolean hasAnyRole(String... roles) {
        if (this.roles == null) {
            return false;
        }
        for (String role : roles) {
            if (this.roles.contains(role)) {
                return true;
            }
        }
        return false;
    }
}