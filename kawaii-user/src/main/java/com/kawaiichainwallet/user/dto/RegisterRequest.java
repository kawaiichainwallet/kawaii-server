package com.kawaiichainwallet.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {

    /**
     * 目标 - 手机号或邮箱
     */
    @NotBlank(message = "请输入手机号或邮箱")
    private String target;

    /**
     * 类型 - phone或email
     */
    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "^(phone|email)$", message = "类型只能是phone或email")
    private String type;

    /**
     * 验证Token (OTP验证通过后获取)
     */
    @NotBlank(message = "请提供验证Token")
    private String verificationToken;

    /**
     * 用户名
     */
    @NotBlank(message = "请输入用户名")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "用户名应为3-20位字母、数字或下划线")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "请输入密码")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "请确认密码")
    private String confirmPassword;

    /**
     * 是否同意用户协议
     */
    @NotNull(message = "请同意用户服务协议")
    private Boolean agreeToTerms;
}