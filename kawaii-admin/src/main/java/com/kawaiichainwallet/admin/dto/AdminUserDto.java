package com.kawaiichainwallet.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员用户详细信息DTO
 *
 * <p><b>时间字段约定</b>：所有时间字段统一使用 UTC 时区</p>
 *
 * @author KawaiiChain
 */
@Data
public class AdminUserDto {

    /**
     * 管理员ID
     */
    private Long adminId;

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
     * 真实姓名
     */
    private String realName;

    /**
     * 员工编号
     */
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
    private Boolean twoFactorEnabled;

    /**
     * 是否超级管理员
     */
    private Boolean isSuperAdmin;

    /**
     * 角色列表
     */
    private List<AdminRoleDto> roles;

    /**
     * 额外权限列表
     */
    private List<String> permissions;

    /**
     * 登录尝试次数
     */
    private Integer loginAttempts;

    /**
     * 锁定截止时间（UTC）
     */
    private LocalDateTime lockedUntil;

    /**
     * 最后登录时间（UTC）
     */
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 创建时间（UTC）
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间（UTC）
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;
}
