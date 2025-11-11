package com.kawaiichainwallet.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理员登录响应DTO
 *
 * @author KawaiiChain
 */
@Data
public class AdminLoginResponse {

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
     * 真实姓名
     */
    private String realName;

    /**
     * 是否超级管理员
     */
    private Boolean isSuperAdmin;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌过期时间（秒）
     */
    private Integer expiresIn;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";
}
