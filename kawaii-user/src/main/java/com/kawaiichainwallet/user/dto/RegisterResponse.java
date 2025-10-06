package com.kawaiichainwallet.user.dto;

import lombok.Data;

/**
 * 注册响应DTO
 */
@Data
public class RegisterResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号（脱敏）
     */
    private String phone;

    /**
     * 邮箱（脱敏）
     */
    private String email;

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