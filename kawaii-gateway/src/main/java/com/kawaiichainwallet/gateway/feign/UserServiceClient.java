package com.kawaiichainwallet.gateway.feign;

import com.kawaiichainwallet.common.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(
    name = "kawaii-user",
    url = "${app.services.user.url:http://localhost:8082}"
)
public interface UserServiceClient {

    /**
     * 验证Token并获取用户信息
     */
    @GetMapping("/validate")
    R<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader);

    /**
     * 根据用户ID获取用户详细信息（包括角色）
     */
    @GetMapping("/users/{userId}")
    R<UserInfo> getUserInfo(@PathVariable("userId") String userId,
                            @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 用户信息DTO
     */
    class UserInfo {
        private String userId;
        private String username;
        private String email;
        private String phone;
        private java.util.List<String> roles;
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

        public java.util.List<String> getRoles() { return roles; }
        public void setRoles(java.util.List<String> roles) { this.roles = roles; }

        public String getKycLevel() { return kycLevel; }
        public void setKycLevel(String kycLevel) { this.kycLevel = kycLevel; }

        public Boolean getIsEmailVerified() { return isEmailVerified; }
        public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

        public Boolean getIsPhoneVerified() { return isPhoneVerified; }
        public void setIsPhoneVerified(Boolean isPhoneVerified) { this.isPhoneVerified = isPhoneVerified; }
    }
}