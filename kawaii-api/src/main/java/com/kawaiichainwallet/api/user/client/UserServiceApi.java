package com.kawaiichainwallet.api.user.client;

import com.kawaiichainwallet.api.user.dto.UserInfoResponse;
import com.kawaiichainwallet.api.user.dto.TokenValidationResponse;
import com.kawaiichainwallet.api.user.dto.UserPaymentPermissionResponse;
import com.kawaiichainwallet.api.user.fallback.UserServiceApiFallbackFactory;
import com.kawaiichainwallet.common.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务API接口定义
 * 该接口由用户服务实现，其他服务通过Feign调用
 */
@FeignClient(
    name = "kawaii-user",
    contextId = "userServiceApi",
        path = "/user/internal/users",
    fallbackFactory = UserServiceApiFallbackFactory.class
)
public interface UserServiceApi {

    /**
     * 验证Token并获取用户信息
     */
    @PostMapping("/validate-token")
    R<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader);

    /**
     * 根据用户ID获取用户信息（内部调用）
     */
    @GetMapping("/{userId}")
    R<UserInfoResponse> getUserInfo(@PathVariable("userId") String userId);

    /**
     * 根据用户名获取用户信息（内部调用）
     */
    @GetMapping("/username/{username}")
    R<UserInfoResponse> getUserByUsername(@PathVariable("username") String username);

    /**
     * 根据邮箱获取用户信息（内部调用）
     */
    @GetMapping("/email/{email}")
    R<UserInfoResponse> getUserByEmail(@PathVariable("email") String email);

    /**
     * 批量获取用户信息（内部调用）
     */
    @PostMapping("/batch")
    R<java.util.List<UserInfoResponse>> getBatchUsers(@RequestBody java.util.List<String> userIds);

    /**
     * 检查用户是否存在
     */
    @GetMapping("/exists/{userId}")
    R<Boolean> userExists(@PathVariable("userId") String userId);

    /**
     * 获取用户的支付权限信息
     */
    @GetMapping("/{userId}/payment-permission")
    R<UserPaymentPermissionResponse> getUserPaymentPermission(@PathVariable("userId") String userId);

}