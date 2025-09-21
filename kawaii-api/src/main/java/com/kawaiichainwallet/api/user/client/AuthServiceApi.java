package com.kawaiichainwallet.api.user.client;

import com.kawaiichainwallet.api.user.dto.TokenValidationResponse;
import com.kawaiichainwallet.api.user.fallback.AuthServiceApiFallbackFactory;
import com.kawaiichainwallet.common.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 认证服务API接口定义
 * 该接口由用户服务实现，其他服务通过Feign调用
 */
@FeignClient(
    name = "kawaii-user",
    contextId = "authServiceApi",
    path = "/user/internal/auth",
    fallbackFactory = AuthServiceApiFallbackFactory.class
)
public interface AuthServiceApi {

    /**
     * 验证Token（内部调用）
     */
    @PostMapping("/validate-token")
    R<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader,
                                           @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 验证用户是否已认证（内部调用）
     */
    @GetMapping("/check-authentication/{userId}")
    R<Boolean> checkAuthentication(@PathVariable("userId") String userId,
                                 @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 撤销用户的所有Token（内部调用，用于安全事件响应）
     */
    @PostMapping("/revoke-tokens/{userId}")
    R<Void> revokeUserTokens(@PathVariable("userId") String userId,
                           @RequestParam("reason") String reason,
                           @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 检查用户密码是否正确（内部调用，用于敏感操作验证）
     */
    @PostMapping("/verify-password")
    R<Boolean> verifyPassword(@RequestParam("userId") String userId,
                            @RequestParam("password") String password,
                            @RequestHeader("X-Internal-Token") String internalToken);
}