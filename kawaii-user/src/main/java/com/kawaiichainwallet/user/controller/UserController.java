package com.kawaiichainwallet.user.controller;

import com.kawaiichainwallet.common.enums.ApiCode;
import com.kawaiichainwallet.common.response.ApiResponse;
import com.kawaiichainwallet.user.context.UserContextHolder;
import com.kawaiichainwallet.user.dto.RegisterResponse;
import com.kawaiichainwallet.user.dto.SendOtpRequest;
import com.kawaiichainwallet.user.dto.UserInfoResponse;
import com.kawaiichainwallet.user.dto.VerifyOtpRequest;
import com.kawaiichainwallet.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、信息管理相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 发送注册验证码
     */
    @PostMapping("/register/send-otp")
    @Operation(summary = "发送注册验证码", description = "向用户手机号或邮箱发送注册验证码")
    public ApiResponse<String> sendRegisterOtp(@Valid @RequestBody SendOtpRequest request) {
        log.info("发送注册验证码请求: target={}, type={}", request.getTarget(), request.getType());
        userService.sendRegisterOtp(request);
        return ApiResponse.success("验证码发送成功");
    }

    /**
     * 验证OTP并完成注册
     */
    @PostMapping("/register/verify-otp")
    @Operation(summary = "验证OTP并注册", description = "验证OTP验证码并完成用户注册")
    public ApiResponse<RegisterResponse> registerWithOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("用户注册请求: username={}, type={}", request.getUsername(), request.getType());
        RegisterResponse response = userService.registerWithOtp(request);
        return ApiResponse.success("注册成功", response);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<UserInfoResponse> getUserInfo() {
        // 从用户上下文中获取当前用户ID
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(ApiCode.UNAUTHORIZED, "用户未认证");
        }

        log.info("获取用户信息请求: userId={}, email={}, roles={}",
                userId,
                UserContextHolder.getCurrentUserEmail(),
                UserContextHolder.getCurrentUserRoles());

        UserInfoResponse response = userService.getUserInfo(userId);
        return ApiResponse.success(response);
    }
}