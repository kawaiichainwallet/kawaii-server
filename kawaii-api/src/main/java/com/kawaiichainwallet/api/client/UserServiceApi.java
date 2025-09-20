package com.kawaiichainwallet.api.client;

import com.kawaiichainwallet.api.dto.UserInfoDto;
import com.kawaiichainwallet.api.dto.TokenValidationDto;
import com.kawaiichainwallet.common.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务API接口定义
 * 该接口由用户服务实现，其他服务通过Feign调用
 */
@FeignClient(
    name = "kawaii-user",
    path = "/api/v1",
    contextId = "userServiceApi"
)
public interface UserServiceApi {

    /**
     * 验证Token并获取用户信息
     */
    @PostMapping("/internal/validate-token")
    R<TokenValidationDto> validateToken(@RequestHeader("Authorization") String authHeader,
                                       @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 根据用户ID获取用户信息（内部调用）
     */
    @GetMapping("/internal/users/{userId}")
    R<UserInfoDto> getUserInfo(@PathVariable("userId") String userId,
                              @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 根据用户名获取用户信息（内部调用）
     */
    @GetMapping("/internal/users/username/{username}")
    R<UserInfoDto> getUserByUsername(@PathVariable("username") String username,
                                    @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 根据邮箱获取用户信息（内部调用）
     */
    @GetMapping("/internal/users/email/{email}")
    R<UserInfoDto> getUserByEmail(@PathVariable("email") String email,
                                 @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 批量获取用户信息（内部调用）
     */
    @PostMapping("/internal/users/batch")
    R<java.util.List<UserInfoDto>> getBatchUsers(@RequestBody java.util.List<String> userIds,
                                                 @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 检查用户是否存在
     */
    @GetMapping("/internal/users/exists/{userId}")
    R<Boolean> userExists(@PathVariable("userId") String userId,
                         @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 获取用户的支付权限信息
     */
    @GetMapping("/internal/users/{userId}/payment-permission")
    R<UserPaymentPermissionDto> getUserPaymentPermission(@PathVariable("userId") String userId,
                                                         @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 用户支付权限DTO
     */
    class UserPaymentPermissionDto {
        private Boolean paymentEnabled;
        private String kycLevel;
        private Boolean emailVerified;
        private Boolean phoneVerified;
        private Boolean twoFactorEnabled;
        private java.math.BigDecimal dailyLimit;
        private java.math.BigDecimal monthlyLimit;

        // Getters and Setters
        public Boolean getPaymentEnabled() { return paymentEnabled; }
        public void setPaymentEnabled(Boolean paymentEnabled) { this.paymentEnabled = paymentEnabled; }

        public String getKycLevel() { return kycLevel; }
        public void setKycLevel(String kycLevel) { this.kycLevel = kycLevel; }

        public Boolean getEmailVerified() { return emailVerified; }
        public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

        public Boolean getPhoneVerified() { return phoneVerified; }
        public void setPhoneVerified(Boolean phoneVerified) { this.phoneVerified = phoneVerified; }

        public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
        public void setTwoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

        public java.math.BigDecimal getDailyLimit() { return dailyLimit; }
        public void setDailyLimit(java.math.BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }

        public java.math.BigDecimal getMonthlyLimit() { return monthlyLimit; }
        public void setMonthlyLimit(java.math.BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
    }
}