package com.kawaiichainwallet.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户详细信息DTO - 用于用户服务内部业务逻辑
 */
@Data
public class UserDetailsDto {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱（脱敏）
     */
    private String email;

    /**
     * 手机号（脱敏）
     */
    private String phone;

    /**
     * 用户状态
     */
    private String status;

    /**
     * 邮箱是否已验证
     */
    private Boolean emailVerified;

    /**
     * 手机号是否已验证
     */
    private Boolean phoneVerified;

    /**
     * 是否启用双因子认证
     */
    private Boolean twoFactorEnabled;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 语言偏好
     */
    private String language;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 货币偏好
     */
    private String currency;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
}