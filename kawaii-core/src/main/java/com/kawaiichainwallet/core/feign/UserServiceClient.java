package com.kawaiichainwallet.core.feign;

import com.kawaiichainwallet.common.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户服务Feign客户端 - 钱包服务调用
 */
@FeignClient(
    name = "kawaii-user",
    url = "${app.services.user.url:http://localhost:8082}"
)
public interface UserServiceClient {

    /**
     * 获取用户基本信息（用于钱包操作验证）
     */
    @GetMapping("/api/v1/users/{userId}")
    R<UserBasicInfo> getUserBasicInfo(@PathVariable("userId") String userId,
                                      @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 用户基本信息DTO
     */
    class UserBasicInfo {
        private String userId;
        private String username;
        private String email;
        private String phone;
        private String kycLevel;
        private Boolean isEmailVerified;
        private Boolean isPhoneVerified;

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
    }
}