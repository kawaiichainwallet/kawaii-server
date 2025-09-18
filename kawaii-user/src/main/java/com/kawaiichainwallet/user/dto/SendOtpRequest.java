package com.kawaiichainwallet.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 发送OTP验证码请求
 */
@Data
public class SendOtpRequest {

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
     * 业务类型 (register, login, reset_password)
     */
    @NotBlank(message = "业务类型不能为空")
    @Pattern(regexp = "^(register|login|reset_password)$", message = "业务类型无效")
    private String purpose;
}