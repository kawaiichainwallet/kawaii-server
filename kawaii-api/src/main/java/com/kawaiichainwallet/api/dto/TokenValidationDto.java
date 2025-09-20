package com.kawaiichainwallet.api.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Token验证结果DTO
 */
@Data
public class TokenValidationDto {

    /**
     * Token是否有效
     */
    private Boolean valid;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色
     */
    private List<String> roles;

    /**
     * Token类型 (access, refresh)
     */
    private String tokenType;

    /**
     * Token过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 错误信息（如果无效）
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;
}