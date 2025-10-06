package com.kawaiichainwallet.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证OTP响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpResponse {

    /**
     * 验证是否成功
     */
    private Boolean valid;

    /**
     * 验证消息
     */
    private String message;

    /**
     * 验证通过后的临时token（可选，用于后续步骤验证）
     */
    private String verificationToken;

    public static VerifyOtpResponse success(String verificationToken) {
        return new VerifyOtpResponse(true, "验证成功", verificationToken);
    }

}
