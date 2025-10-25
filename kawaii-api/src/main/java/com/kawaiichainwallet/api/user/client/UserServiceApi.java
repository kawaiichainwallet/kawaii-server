package com.kawaiichainwallet.api.user.client;

import com.kawaiichainwallet.api.user.dto.UserInfoResponse;
import com.kawaiichainwallet.api.user.dto.UserPaymentPermissionResponse;
import com.kawaiichainwallet.api.user.fallback.UserServiceApiFallbackFactory;
import com.kawaiichainwallet.common.core.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
     * 根据用户ID获取用户信息（内部调用）
     */
    @GetMapping("/{userId}")
    R<UserInfoResponse> getUserInfo(@PathVariable("userId") long userId);

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
    R<Boolean> userExists(@PathVariable("userId") long userId);

    /**
     * 获取用户的支付权限信息
     */
    @GetMapping("/{userId}/payment-permission")
    R<UserPaymentPermissionResponse> getUserPaymentPermission(@PathVariable("userId") String userId);

}