package com.kawaiichainwallet.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * OTP登录请求DTO
 */
@Data
public class OtpLoginRequest {

    /**
     * 手机号
     */
    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * OTP验证码
     */
    @NotBlank(message = "请输入验证码")
    @Pattern(regexp = "^\\d{6}$", message = "验证码应为6位数字")
    private String otpCode;
}