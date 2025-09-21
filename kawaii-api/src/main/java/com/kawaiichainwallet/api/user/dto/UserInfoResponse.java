package com.kawaiichainwallet.api.user.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息响应 - 服务间调用统一数据传输对象
 */
@Data
public class UserInfoResponse {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱（已脱敏）
     */
    private String email;

    /**
     * 手机号（已脱敏）
     */
    private String phone;

    /**
     * 用户状态 (active, inactive, suspended, deleted)
     */
    private String status;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * KYC认证级别
     */
    private String kycLevel;

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
     * 是否启用支付功能
     */
    private Boolean paymentEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
}