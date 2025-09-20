package com.kawaiichainwallet.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kawaiichainwallet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 用户实体类 - 对应 users 表（包含认证和用户信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 8799985057427895763L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.ASSIGN_UUID)
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
    @TableField("password_hash")
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
    @TableField("email_verified")
    private Boolean emailVerified;

    /**
     * 手机号是否已验证
     */
    @TableField("phone_verified")
    private Boolean phoneVerified;

    /**
     * 是否启用双因子认证
     */
    @TableField("two_factor_enabled")
    private Boolean twoFactorEnabled;

    /**
     * 双因子认证密钥
     */
    @TableField("two_factor_secret")
    private String twoFactorSecret;

    /**
     * 登录尝试次数
     */
    @TableField("login_attempts")
    private Integer loginAttempts;

    /**
     * 锁定截止时间
     */
    @TableField("locked_until")
    private LocalDateTime lockedUntil;

    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;
}