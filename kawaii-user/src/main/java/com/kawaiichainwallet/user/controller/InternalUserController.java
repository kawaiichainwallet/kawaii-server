package com.kawaiichainwallet.user.controller;

import com.kawaiichainwallet.api.user.client.UserServiceApi;
import com.kawaiichainwallet.api.user.dto.TokenValidationResponse;
import com.kawaiichainwallet.api.user.dto.UserInfoResponse;
import com.kawaiichainwallet.api.user.dto.UserPaymentPermissionResponse;
import com.kawaiichainwallet.common.enums.ApiCode;
import com.kawaiichainwallet.common.response.R;
import com.kawaiichainwallet.user.converter.ServiceApiConverter;
import com.kawaiichainwallet.user.service.AuthService;
import com.kawaiichainwallet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部用户API控制器 - 实现UserServiceApi接口
 * 仅供内部服务调用
 */
@Slf4j
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController implements UserServiceApi {

    private final UserService userService;
    private final AuthService authService;
    private final ServiceApiConverter serviceApiConverter;

    @Override
    public R<TokenValidationResponse> validateToken(String authHeader) {
        log.info("内部服务Token验证请求");

        try {
            // 验证内部调用Token

            // 调用认证服务验证Token
            var validationResponse = authService.validateToken(authHeader);

            // 转换为API DTO
            TokenValidationResponse dto = convertToTokenValidationResponse(validationResponse);

            return R.success(dto);
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            TokenValidationResponse errorDto = serviceApiConverter.createFailedValidationResponse(
                    e.getMessage(), "VALIDATION_ERROR");
            return R.success(errorDto);
        }
    }

    @Override
    public R<UserInfoResponse> getUserInfo(String userId) {
        log.info("内部服务获取用户信息请求: userId={}", userId);

        try {
            // 验证内部调用Token

            // 获取用户信息
            var userDetailsDto = userService.getUserInfo(userId);

            // 转换为API DTO
            UserInfoResponse dto = serviceApiConverter.userDetailsToApiDto(userDetailsDto);

            return R.success(dto);
        } catch (Exception e) {
            log.error("获取用户信息失败: userId={}, error={}", userId, e.getMessage());
            return R.error(ApiCode.USER_INFO_FETCH_FAILED);
        }
    }

    @Override
    public R<UserInfoResponse> getUserByUsername(String username) {
        log.info("内部服务根据用户名获取用户信息: username={}", username);

        try {

            var user = userService.getUserByUsername(username);
            if (user == null) {
                return R.error(ApiCode.USER_NOT_FOUND);
            }

            // 使用MapStruct转换用户信息
            UserInfoResponse dto = serviceApiConverter.userToApiDto(user);

            return R.success(dto);
        } catch (Exception e) {
            log.error("根据用户名获取用户信息失败: username={}, error={}", username, e.getMessage());
            return R.error(ApiCode.USER_INFO_FETCH_FAILED);
        }
    }

    @Override
    public R<UserInfoResponse> getUserByEmail(String email) {
        log.info("内部服务根据邮箱获取用户信息: email={}", email);

        try {

            var user = userService.getUserByEmail(email);
            if (user == null) {
                return R.error(ApiCode.USER_NOT_FOUND);
            }

            // 使用MapStruct转换用户信息
            UserInfoResponse dto = serviceApiConverter.userToApiDto(user);

            return R.success(dto);
        } catch (Exception e) {
            log.error("根据邮箱获取用户信息失败: email={}, error={}", email, e.getMessage());
            return R.error(ApiCode.USER_INFO_FETCH_FAILED);
        }
    }

    @Override
    public R<List<UserInfoResponse>> getBatchUsers(List<String> userIds) {
        log.info("内部服务批量获取用户信息: userIds={}", userIds);

        try {

            // TODO: 实现批量获取用户信息逻辑
            List<UserInfoResponse> users = List.of(); // 暂时返回空列表

            return R.success(users);
        } catch (Exception e) {
            log.error("批量获取用户信息失败: userIds={}, error={}", userIds, e.getMessage());
            return R.error(ApiCode.BATCH_USER_FETCH_FAILED);
        }
    }

    @Override
    public R<Boolean> userExists(String userId) {
        log.info("内部服务检查用户是否存在: userId={}", userId);

        try {

            var user = userService.getUserById(userId);
            boolean exists = user != null;

            return R.success(exists);
        } catch (Exception e) {
            log.error("检查用户是否存在失败: userId={}, error={}", userId, e.getMessage());
            return R.success(false); // 出错时返回false
        }
    }

    @Override
    public R<UserPaymentPermissionResponse> getUserPaymentPermission(String userId) {
        log.info("内部服务获取用户支付权限: userId={}", userId);

        try {

            // TODO: 实现获取用户支付权限逻辑
            var permission = new UserPaymentPermissionResponse();
            permission.setPaymentEnabled(true);
            permission.setKycLevel("BASIC");
            // 其他字段设置...

            return R.success(permission);
        } catch (Exception e) {
            log.error("获取用户支付权限失败: userId={}, error={}", userId, e.getMessage());
            return R.error(ApiCode.USER_PAYMENT_PERMISSION_FETCH_FAILED);
        }
    }

    // ===========================================
    // 私有辅助方法
    // ===========================================

    /**
     * 验证内部调用Token
     */

    /**
     * 转换TokenValidationResponse为TokenValidationResponse
     */
    private TokenValidationResponse convertToTokenValidationResponse(Object validationResponse) {
        // 使用MapStruct转换，如果validationResponse是TokenValidationResponse类型
        if (validationResponse instanceof com.kawaiichainwallet.user.dto.TokenValidationResponse response) {
            return serviceApiConverter.validationResponseToApiDto(response);
        }
        // 默认返回成功状态
        return serviceApiConverter.createSuccessValidationResponse(null, null, null);
    }
}