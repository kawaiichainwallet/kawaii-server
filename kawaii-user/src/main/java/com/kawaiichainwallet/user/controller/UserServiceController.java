package com.kawaiichainwallet.user.controller;

import com.kawaiichainwallet.api.client.UserServiceApi;
import com.kawaiichainwallet.api.dto.UserInfoResponse;
import com.kawaiichainwallet.api.dto.TokenValidationResponse;
import com.kawaiichainwallet.api.dto.UserPaymentPermissionResponse;
import com.kawaiichainwallet.common.response.R;
import com.kawaiichainwallet.user.service.UserService;
import com.kawaiichainwallet.user.service.AuthService;
import com.kawaiichainwallet.user.converter.UserConverter;
import com.kawaiichainwallet.user.converter.ServiceApiConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户服务API控制器 - 实现UserServiceApi接口
 * 提供标准化的服务间调用接口
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserServiceController implements UserServiceApi {

    private final UserService userService;
    private final AuthService authService;
    private final UserConverter userConverter;
    private final ServiceApiConverter serviceApiConverter;

    @Override
    public R<TokenValidationResponse> validateToken(String authHeader, String internalToken) {
        log.info("内部服务Token验证请求");

        try {
            // 验证内部调用Token
            if (!isValidInternalToken(internalToken)) {
                log.warn("无效的内部调用Token");
                return R.error(401, "无效的内部调用Token");
            }

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
    public R<UserInfoResponse> getUserInfo(String userId, String internalToken) {
        log.info("内部服务获取用户信息请求: userId={}", userId);

        try {
            // 验证内部调用Token
            if (!isValidInternalToken(internalToken)) {
                return R.error(401, "无效的内部调用Token");
            }

            // 获取用户信息
            var userInfoResponse = userService.getUserInfo(userId);

            // 转换为API DTO
            UserInfoResponse dto = convertToUserInfoResponse(userInfoResponse);

            return R.success(dto);
        } catch (Exception e) {
            log.error("获取用户信息失败: userId={}, error={}", userId, e.getMessage());
            return R.error(500, "获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public R<UserInfoResponse> getUserByUsername(String username, String internalToken) {
        log.info("内部服务根据用户名获取用户信息: username={}", username);

        try {
            if (!isValidInternalToken(internalToken)) {
                return R.error(401, "无效的内部调用Token");
            }

            var user = userService.getUserByUsername(username);
            if (user == null) {
                return R.error(404, "用户不存在");
            }

            // 使用MapStruct转换用户信息
            UserInfoResponse dto = serviceApiConverter.userToUserInfoResponse(user);

            return R.success(dto);
        } catch (Exception e) {
            log.error("根据用户名获取用户信息失败: username={}, error={}", username, e.getMessage());
            return R.error(500, "获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public R<UserInfoResponse> getUserByEmail(String email, String internalToken) {
        log.info("内部服务根据邮箱获取用户信息: email={}", email);

        try {
            if (!isValidInternalToken(internalToken)) {
                return R.error(401, "无效的内部调用Token");
            }

            var user = userService.getUserByEmail(email);
            if (user == null) {
                return R.error(404, "用户不存在");
            }

            // 使用MapStruct转换用户信息
            UserInfoResponse dto = serviceApiConverter.userToUserInfoResponse(user);

            return R.success(dto);
        } catch (Exception e) {
            log.error("根据邮箱获取用户信息失败: email={}, error={}", email, e.getMessage());
            return R.error(500, "获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public R<List<UserInfoResponse>> getBatchUsers(List<String> userIds, String internalToken) {
        log.info("内部服务批量获取用户信息: userIds={}", userIds);

        try {
            if (!isValidInternalToken(internalToken)) {
                return R.error(401, "无效的内部调用Token");
            }

            // TODO: 实现批量获取用户信息逻辑
            List<UserInfoResponse> users = List.of(); // 暂时返回空列表

            return R.success(users);
        } catch (Exception e) {
            log.error("批量获取用户信息失败: userIds={}, error={}", userIds, e.getMessage());
            return R.error(500, "批量获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> userExists(String userId, String internalToken) {
        log.info("内部服务检查用户是否存在: userId={}", userId);

        try {
            if (!isValidInternalToken(internalToken)) {
                return R.error(401, "无效的内部调用Token");
            }

            var user = userService.getUserById(userId);
            boolean exists = user != null;

            return R.success(exists);
        } catch (Exception e) {
            log.error("检查用户是否存在失败: userId={}, error={}", userId, e.getMessage());
            return R.success(false); // 出错时返回false
        }
    }

    @Override
    public R<UserPaymentPermissionResponse> getUserPaymentPermission(String userId, String internalToken) {
        log.info("内部服务获取用户支付权限: userId={}", userId);

        try {
            if (!isValidInternalToken(internalToken)) {
                return R.error(401, "无效的内部调用Token");
            }

            // TODO: 实现获取用户支付权限逻辑
            var permission = new UserPaymentPermissionResponse();
            permission.setPaymentEnabled(true);
            permission.setKycLevel("BASIC");
            // 其他字段设置...

            return R.success(permission);
        } catch (Exception e) {
            log.error("获取用户支付权限失败: userId={}, error={}", userId, e.getMessage());
            return R.error(500, "获取用户支付权限失败: " + e.getMessage());
        }
    }

    /**
     * 验证内部调用Token
     */
    private boolean isValidInternalToken(String internalToken) {
        // TODO: 实现内部Token验证逻辑
        // 这里可以验证Token格式、签名、时间戳等
        return internalToken != null && !internalToken.trim().isEmpty();
    }

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

    /**
     * 转换UserInfoResponse为UserInfoResponse
     */
    private UserInfoResponse convertToUserInfoResponse(Object userInfoResponse) {
        // TODO: 实现具体转换逻辑，这里可以使用MapStruct
        UserInfoResponse dto = new UserInfoResponse();
        // 字段转换...
        return dto;
    }
}