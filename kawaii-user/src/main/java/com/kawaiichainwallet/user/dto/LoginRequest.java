package com.kawaiichainwallet.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {

    /**
     * 标识符 - 用户名、邮箱或手机号
     */
    @NotBlank(message = "请输入用户名、邮箱或手机号")
    private String identifier;

    /**
     * 密码
     */
    @NotBlank(message = "请输入密码")
    private String password;
}