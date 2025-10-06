package com.kawaiichainwallet.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 验证OTP请求DTO
 */
@Data
public class VerifyOtpRequest {

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
     * OTP验证码
     */
    @NotBlank(message = "请输入验证码")
    @Pattern(regexp = "^\\d{6}$", message = "验证码应为6位数字")
    private String otpCode;

    /**
     * 用途 - register, login, reset_password
     */
    @NotBlank(message = "用途不能为空")
    @Pattern(regexp = "^(register|login|reset_password)$", message = "用途不正确")
    private String purpose;
}
