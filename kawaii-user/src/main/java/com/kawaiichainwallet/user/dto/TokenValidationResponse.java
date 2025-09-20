package com.kawaiichainwallet.user.dto;

import lombok.Data;

/**
 * Token验证响应
 */
@Data
public class TokenValidationResponse {

    /**
     * Token是否有效
     */
    private boolean valid;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * Token过期时间（时间戳）
     */
    private Long expiresAt;

    /**
     * Token类型
     */
    private String tokenType;

    /**
     * 错误信息（当valid=false时）
     */
    private String errorMessage;
}