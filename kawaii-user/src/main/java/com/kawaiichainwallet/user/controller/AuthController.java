package com.kawaiichainwallet.user.controller;

import com.kawaiichainwallet.user.dto.*;
import com.kawaiichainwallet.user.service.AuthService;
import com.kawaiichainwallet.common.response.R;
import com.kawaiichainwallet.common.utils.RequestUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 - 集成在用户服务中
 */
@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户名/邮箱/手机号 + 密码登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持用户名/邮箱/手机号 + 密码登录")
    public R<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = RequestUtil.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, clientIp, userAgent);
        return R.success(response, "登录成功");
    }

    /**
     * 手机验证码登录
     */
    @PostMapping("/login/otp")
    @Operation(summary = "验证码登录", description = "使用手机号 + 验证码登录")
    public R<LoginResponse> loginWithOtp(
            @Valid @RequestBody OtpLoginRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = RequestUtil.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.loginWithOtp(request, clientIp, userAgent);
        return R.success(response, "登录成功");
    }

    /**
     * 发送登录验证码
     */
    @PostMapping("/send-login-otp")
    @Operation(summary = "发送登录验证码", description = "向手机号发送登录验证码")
    public R<Void> sendLoginOtp(
            @Valid @RequestBody SendOtpRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = RequestUtil.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        authService.sendLoginOtp(request.getTarget(), clientIp, userAgent);
        return R.success("验证码发送成功");
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用Refresh Token获取新的Access Token")
    public R<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = RequestUtil.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.refreshToken(request, clientIp, userAgent);
        return R.success(response, "Token刷新成功");
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户主动登出")
    public R<Void> logout(HttpServletRequest httpRequest) {
        String clientIp = RequestUtil.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String userId = RequestUtil.getCurrentUserId();

        authService.logout(userId, clientIp, userAgent);
        return R.success("登出成功");
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "通过邮箱或手机号注册新用户")
    public R<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = RequestUtil.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        RegisterResponse response = authService.register(request, clientIp, userAgent);
        return R.success(response, "注册成功");
    }

    /**
     * 发送注册验证码
     */
    @PostMapping("/send-register-otp")
    @Operation(summary = "发送注册验证码", description = "向邮箱或手机号发送注册验证码")
    public R<Void> sendRegisterOtp(
            @Valid @RequestBody SendOtpRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = RequestUtil.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        authService.sendRegisterOtp(request.getTarget(), request.getType(), clientIp, userAgent);
        return R.success("验证码发送成功");
    }

    /**
     * 验证Token
     */
    @GetMapping("/validate")
    @Operation(summary = "验证Token", description = "验证Access Token是否有效")
    public R<TokenValidationResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractTokenFromAuthHeader(authHeader);
        TokenValidationResponse response = authService.validateToken(token);
        return R.success(response, "Token验证完成");
    }

    /**
     * 从Authorization header中提取Token
     */
    private String extractTokenFromAuthHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}