package com.kawaiichainwallet.user.dto;

import lombok.Data;

/**
 * 注册响应
 */
@Data
public class RegisterResponse {

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
    private Long expiresIn;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 是否首次注册
     */
    private Boolean isFirstTimeUser = true;
}