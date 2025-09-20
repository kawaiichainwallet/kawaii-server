package com.kawaiichainwallet.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送OTP请求DTO
 */
@Data
public class SendOtpRequest {

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
     * 用途 - register, login, reset_password
     */
    @NotBlank(message = "用途不能为空")
    @Pattern(regexp = "^(register|login|reset_password)$", message = "用途不正确")
    private String purpose;
}