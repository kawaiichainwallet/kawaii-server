package com.kawaiichainwallet.admin.controller;

import com.kawaiichainwallet.admin.dto.AdminLoginRequest;
import com.kawaiichainwallet.admin.dto.AdminLoginResponse;
import com.kawaiichainwallet.admin.service.AdminAuthService;
import com.kawaiichainwallet.common.core.response.R;
import com.kawaiichainwallet.common.spring.utils.RequestUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员认证控制器 - 提供管理后台认证API
 *
 * @author KawaiiChain
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "管理员认证接口", description = "管理后台认证相关接口")
public class AuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "使用用户名/邮箱 + 密码登录")
    public R<AdminLoginResponse> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = RequestUtil.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AdminLoginResponse response = adminAuthService.login(request, clientIp, userAgent);
        return R.success(response, "登录成功");
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    @Operation(summary = "管理员登出", description = "管理员主动登出")
    public R<Void> logout(HttpServletRequest httpRequest) {
        String clientIp = RequestUtil.getClientIpAddress(httpRequest);
        Long adminId = RequestUtil.getCurrentUserId();

        // 提取 Token
        String authHeader = httpRequest.getHeader("Authorization");
        String token = extractTokenFromAuthHeader(authHeader);

        adminAuthService.logout(adminId, token, clientIp);
        return R.success("登出成功");
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用Refresh Token获取新的Access Token")
    public R<AdminLoginResponse> refreshToken(
            @RequestBody String refreshToken,
            HttpServletRequest httpRequest) {

        String clientIp = RequestUtil.getClientIpAddress(httpRequest);

        AdminLoginResponse response = adminAuthService.refreshToken(refreshToken, clientIp);
        return R.success(response, "Token刷新成功");
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
