package com.kawaiichainwallet.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 验证OTP并注册请求
 */
@Data
public class VerifyOtpRequest {

    /**
     * 接收验证码的目标（手机号或邮箱）
     */
    @NotBlank(message = "接收目标不能为空")
    private String target;

    /**
     * 验证码类型 (phone, email)
     */
    @NotBlank(message = "验证码类型不能为空")
    @Pattern(regexp = "^(phone|email)$", message = "验证码类型只能是phone或email")
    private String type;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    private String otpCode;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20位之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8-20位之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]+$",
             message = "密码必须包含至少一个大写字母、一个小写字母和一个数字")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 是否同意用户协议
     */
    private Boolean agreeToTerms;
}