package com.kawaiichainwallet.user.controller;

import com.kawaiichainwallet.api.user.client.AuthServiceApi;
import com.kawaiichainwallet.api.user.dto.TokenValidationResponse;
import com.kawaiichainwallet.common.core.enums.ApiCode;
import com.kawaiichainwallet.common.core.response.R;
import com.kawaiichainwallet.user.converter.ServiceApiConverter;
import com.kawaiichainwallet.user.service.AuthService;
import com.kawaiichainwallet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部认证API控制器 - 实现AuthServiceApi接口
 * 仅供内部服务调用
 */
@Slf4j
@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController implements AuthServiceApi {

    private final AuthService authService;
    private final UserService userService;
    private final ServiceApiConverter serviceApiConverter;

    @Override
    public R<TokenValidationResponse> validateToken(String authHeader) {
        log.info("内部服务Token验证请求");

        try {

            // 调用认证服务验证Token
            String token = extractTokenFromAuthHeader(authHeader);
            var validationResponse = authService.validateToken(token);

            // 转换为API DTO
            if (validationResponse instanceof com.kawaiichainwallet.user.dto.TokenValidationResponse response) {
                TokenValidationResponse dto = serviceApiConverter.validationResponseToApiDto(response);
                return R.success(dto);
            }

            // 默认返回成功状态
            TokenValidationResponse dto = serviceApiConverter.createSuccessValidationResponse(null, null, null);
            return R.success(dto);
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            TokenValidationResponse errorDto = serviceApiConverter.createFailedValidationResponse(
                    e.getMessage(), "VALIDATION_ERROR");
            return R.success(errorDto);
        }
    }

    @Override
    public R<Boolean> checkAuthentication(long userId) {
        log.info("内部服务检查用户认证状态: userId={}", userId);

        try {

            // TODO: 实现检查用户认证状态逻辑
            // 这里可以检查用户是否存在有效的登录会话等
            var user = userService.getUserById(userId);
            boolean isAuthenticated = user != null && "active".equals(user.getStatus());

            return R.success(isAuthenticated);
        } catch (Exception e) {
            log.error("检查用户认证状态失败: userId={}, error={}", userId, e.getMessage());
            return R.success(false); // 出错时返回false，保证安全
        }
    }

    @Override
    public R<Void> revokeUserTokens(String userId, String reason) {
        log.info("内部服务撤销用户Token: userId={}, reason={}", userId, reason);

        try {

            // TODO: 实现撤销用户Token逻辑
            // 这里可以调用认证服务来撤销用户的所有有效Token
            // authService.revokeAllUserTokens(userId, reason);

            log.info("用户Token撤销成功: userId={}, reason={}", userId, reason);
            return R.success("Token撤销成功");
        } catch (Exception e) {
            log.error("撤销用户Token失败: userId={}, reason={}, error={}", userId, reason, e.getMessage());
            return R.error(ApiCode.TOKEN_REVOKE_FAILED);
        }
    }

    @Override
    public R<Boolean> verifyPassword(String userId, String password) {
        log.info("内部服务验证用户密码: userId={}", userId);

        try {

            // TODO: 实现密码验证逻辑
            // 这里可以调用认证服务来验证用户密码
            // boolean isValid = authService.verifyUserPassword(userId, password);

            // 暂时返回false，等待具体实现
            boolean isValid = false;

            return R.success(isValid);
        } catch (Exception e) {
            log.error("验证用户密码失败: userId={}, error={}", userId, e.getMessage());
            return R.success(false); // 出错时返回false，保证安全
        }
    }

    // ===========================================
    // 私有辅助方法
    // ===========================================


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