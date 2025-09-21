package com.kawaiichainwallet.api.user.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 用户支付权限响应
 * 用于微服务间查询用户支付相关权限信息
 */
@Data
public class UserPaymentPermissionResponse {

    /**
     * 是否启用支付功能
     */
    private Boolean paymentEnabled;

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
     * 日限额
     */
    private BigDecimal dailyLimit;

    /**
     * 月限额
     */
    private BigDecimal monthlyLimit;
}