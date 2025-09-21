package com.kawaiichainwallet.payment.feign;

import com.kawaiichainwallet.common.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户服务Feign客户端 - 支付服务调用
 */
@FeignClient(
    name = "kawaii-user",
    url = "${app.services.user.url:http://localhost:8082}"
)
public interface UserServiceClient {

    /**
     * 获取用户支付信息（用于支付验证）
     */
    @GetMapping("/users/{userId}")
    R<UserPaymentInfo> getUserPaymentInfo(@PathVariable("userId") String userId,
                                          @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 用户支付信息DTO
     */
    class UserPaymentInfo {
        private String userId;
        private String username;
        private String email;
        private String phone;
        private String kycLevel;
        private Boolean isEmailVerified;
        private Boolean isPhoneVerified;
        private Boolean paymentEnabled;

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getKycLevel() { return kycLevel; }
        public void setKycLevel(String kycLevel) { this.kycLevel = kycLevel; }

        public Boolean getIsEmailVerified() { return isEmailVerified; }
        public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

        public Boolean getIsPhoneVerified() { return isPhoneVerified; }
        public void setIsPhoneVerified(Boolean isPhoneVerified) { this.isPhoneVerified = isPhoneVerified; }

        public Boolean getPaymentEnabled() { return paymentEnabled; }
        public void setPaymentEnabled(Boolean paymentEnabled) { this.paymentEnabled = paymentEnabled; }
    }
}