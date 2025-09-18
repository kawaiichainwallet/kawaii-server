package com.kawaiichainwallet.user.entity;

import com.kawaiichainwallet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 用户实体类 - 对应 users 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 8799985057427895763L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码哈希
     */
    private String passwordHash;

    /**
     * 盐值
     */
    private String salt;

    /**
     * 用户状态 (active, inactive, suspended, deleted)
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
     * 双因子认证密钥
     */
    private String twoFactorSecret;

    /**
     * 登录尝试次数
     */
    private Integer loginAttempts;

    /**
     * 锁定截止时间
     */
    private LocalDateTime lockedUntil;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;
}