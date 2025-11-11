package com.kawaiichainwallet.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员用户实体类 - 对应 admin_users 表
 *
 * <p><b>时间字段约定</b>：所有时间字段统一使用 UTC 时区</p>
 *
 * @author KawaiiChain
 */
@Data
@TableName(value = "admin_users", autoResultMap = true)
public class AdminUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 4821516377903782856L;

    /**
     * 管理员ID (使用Leaf分布式ID生成器)
     */
    @TableId(value = "admin_id", type = IdType.INPUT)
    private Long adminId;

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
     * 密码哈希 (BCrypt加密，包含内置盐值)
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 员工编号
     */
    @TableField("employee_id")
    private String employeeId;

    /**
     * 部门
     */
    private String department;

    /**
     * 职位
     */
    private String position;

    /**
     * 管理员状态 (active, inactive, suspended)
     */
    private String status;

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
     * 锁定截止时间（UTC）
     */
    @TableField("locked_until")
    private LocalDateTime lockedUntil;

    /**
     * 最后登录时间（UTC）
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 是否超级管理员
     */
    @TableField("is_super_admin")
    private Boolean isSuperAdmin;

    /**
     * 额外权限列表（TEXT[] 数组类型）
     */
    @TableField(value = "permissions", typeHandler = JacksonTypeHandler.class)
    private List<String> permissions;

    /**
     * 创建时间（UTC）
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间（UTC）
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;
}
