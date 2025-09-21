package com.kawaiichainwallet.user.controller;

import com.kawaiichainwallet.api.user.client.UserServiceApi;
import com.kawaiichainwallet.api.user.dto.TokenValidationResponse;
import com.kawaiichainwallet.api.user.dto.UserInfoResponse;
import com.kawaiichainwallet.api.user.dto.UserPaymentPermissionResponse;
import com.kawaiichainwallet.common.enums.ApiCode;
import com.kawaiichainwallet.common.response.R;
import com.kawaiichainwallet.common.utils.ValidationUtil;
import com.kawaiichainwallet.user.context.UserContextHolder;
import com.kawaiichainwallet.user.converter.ServiceApiConverter;
import com.kawaiichainwallet.user.dto.UpdateUserInfoRequest;
import com.kawaiichainwallet.user.dto.UserDetailsDto;
import com.kawaiichainwallet.user.entity.User;
import com.kawaiichainwallet.user.service.AuthService;
import com.kawaiichainwallet.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器 - 既提供HTTP API又实现Feign服务接口
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController implements UserServiceApi {

    private final UserService userService;
    private final AuthService authService;
    private final ServiceApiConverter serviceApiConverter;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public R<UserDetailsDto> getCurrentUserInfo() {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return R.error(ApiCode.UNAUTHORIZED);
        }

        log.info("获取用户信息请求: userId={}", userId);
        UserDetailsDto response = userService.getUserInfo(userId);
        return R.success(response);
    }

    /**
     * 根据用户ID获取用户信息（HTTP API）
     */
    @GetMapping("/{userId}")
    @Operation(summary = "根据ID获取用户信息", description = "管理员或授权用户获取指定用户信息")
    public R<UserDetailsDto> getUserInfoHttp(@PathVariable String userId) {
        log.info("获取指定用户信息请求: userId={}", userId);
        UserDetailsDto response = userService.getUserInfo(userId);
        return R.success(response);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    public R<UserDetailsDto> updateCurrentUserInfo(@Valid @RequestBody UpdateUserInfoRequest request) {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return R.error(ApiCode.UNAUTHORIZED);
        }

        log.info("更新用户信息请求: userId={}", userId);
        UserDetailsDto response = userService.updateUserInfo(userId, request);
        return R.success(response, "用户信息更新成功");
    }

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名查询用户", description = "根据用户名获取用户基本信息")
    public R<User> getUserByUsername(@PathVariable String username) {
        log.info("根据用户名查询用户: username={}", username);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return R.error(ApiCode.USER_NOT_FOUND);
        }
        return R.success(user);
    }

    /**
     * 根据邮箱查询用户
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "根据邮箱查询用户", description = "根据邮箱获取用户基本信息")
    public R<User> getUserByEmail(@PathVariable String email) {
        log.info("根据邮箱查询用户: email={}", ValidationUtil.maskEmail(email));
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return R.error(ApiCode.USER_NOT_FOUND);
        }
        return R.success(user);
    }

    /**
     * 根据手机号查询用户
     */
    @GetMapping("/phone/{phone}")
    @Operation(summary = "根据手机号查询用户", description = "根据手机号获取用户基本信息")
    public R<User> getUserByPhone(@PathVariable String phone) {
        log.info("根据手机号查询用户: phone={}", ValidationUtil.maskPhone(phone));
        User user = userService.getUserByPhone(phone);
        if (user == null) {
            return R.error(ApiCode.USER_NOT_FOUND);
        }
        return R.success(user);
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username/{username}")
    @Operation(summary = "检查用户名是否存在", description = "检查指定用户名是否已被使用")
    public R<Boolean> checkUsernameExists(@PathVariable String username) {
        boolean exists = userService.isUsernameExists(username);
        return R.success(exists);
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email/{email}")
    @Operation(summary = "检查邮箱是否存在", description = "检查指定邮箱是否已被使用")
    public R<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.isEmailExists(email);
        return R.success(exists);
    }

    /**
     * 检查手机号是否存在
     */
    @GetMapping("/check-phone/{phone}")
    @Operation(summary = "检查手机号是否存在", description = "检查指定手机号是否已被使用")
    public R<Boolean> checkPhoneExists(@PathVariable String phone) {
        boolean exists = userService.isPhoneExists(phone);
        return R.success(exists);
    }

    /**
     * 获取用户列表（管理员功能）
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "管理员获取用户列表")
    public R<List<UserDetailsDto>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        log.info("获取用户列表请求: page={}, size={}, status={}", page, size, status);
        List<UserDetailsDto> users = userService.getUserList(page, size, status);
        return R.success(users);
    }

    // ===========================================
    // 实现UserServiceApi接口方法 - 用于内部服务调用
    // ===========================================

    @Override
    @PostMapping("/internal/validate-token")
    public R<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader,
                                                   @RequestHeader("X-Internal-Token") String internalToken) {
        log.info("内部服务Token验证请求");

        try {
            // 验证内部调用Token
            if (!isValidInternalToken(internalToken)) {
                log.warn("无效的内部调用Token");
                return R.error(ApiCode.INTERNAL_TOKEN_INVALID);
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
    @GetMapping("/internal/users/{userId}")
    public R<UserInfoResponse> getUserInfo(@PathVariable("userId") String userId,
                                          @RequestHeader("X-Internal-Token") String internalToken) {
        log.info("内部服务获取用户信息请求: userId={}", userId);

        try {
            // 验证内部调用Token
            if (!isValidInternalToken(internalToken)) {
                return R.error(ApiCode.INTERNAL_TOKEN_INVALID);
            }

            // 获取用户信息
            var userInfoResponse = userService.getUserInfo(userId);

            // 转换为API DTO
            UserInfoResponse dto = serviceApiConverter.userDetailsToApiDto(userInfoResponse);

            return R.success(dto);
        } catch (Exception e) {
            log.error("获取用户信息失败: userId={}, error={}", userId, e.getMessage());
            return R.error(ApiCode.USER_INFO_FETCH_FAILED);
        }
    }

    @Override
    @GetMapping("/internal/users/username/{username}")
    public R<UserInfoResponse> getUserByUsername(@PathVariable("username") String username,
                                                 @RequestHeader("X-Internal-Token") String internalToken) {
        log.info("内部服务根据用户名获取用户信息: username={}", username);

        try {
            if (!isValidInternalToken(internalToken)) {
                return R.error(ApiCode.INTERNAL_TOKEN_INVALID);
            }

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
    @GetMapping("/internal/users/email/{email}")
    public R<UserInfoResponse> getUserByEmail(@PathVariable("email") String email,
                                              @RequestHeader("X-Internal-Token") String internalToken) {
        log.info("内部服务根据邮箱获取用户信息: email={}", email);

        try {
            if (!isValidInternalToken(internalToken)) {
                return R.error(ApiCode.INTERNAL_TOKEN_INVALID);
            }

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
    @PostMapping("/internal/users/batch")
    public R<List<UserInfoResponse>> getBatchUsers(@RequestBody List<String> userIds,
                                                   @RequestHeader("X-Internal-Token") String internalToken) {
        log.info("内部服务批量获取用户信息: userIds={}", userIds);

        try {
            if (!isValidInternalToken(internalToken)) {
                return R.error(ApiCode.INTERNAL_TOKEN_INVALID);
            }

            // TODO: 实现批量获取用户信息逻辑
            List<UserInfoResponse> users = List.of(); // 暂时返回空列表

            return R.success(users);
        } catch (Exception e) {
            log.error("批量获取用户信息失败: userIds={}, error={}", userIds, e.getMessage());
            return R.error(ApiCode.BATCH_USER_FETCH_FAILED);
        }
    }

    @Override
    @GetMapping("/internal/users/exists/{userId}")
    public R<Boolean> userExists(@PathVariable("userId") String userId,
                                 @RequestHeader("X-Internal-Token") String internalToken) {
        log.info("内部服务检查用户是否存在: userId={}", userId);

        try {
            if (!isValidInternalToken(internalToken)) {
                return R.error(ApiCode.INTERNAL_TOKEN_INVALID);
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
    @GetMapping("/internal/users/{userId}/payment-permission")
    public R<UserPaymentPermissionResponse> getUserPaymentPermission(@PathVariable("userId") String userId,
                                                                     @RequestHeader("X-Internal-Token") String internalToken) {
        log.info("内部服务获取用户支付权限: userId={}", userId);

        try {
            if (!isValidInternalToken(internalToken)) {
                return R.error(ApiCode.INTERNAL_TOKEN_INVALID);
            }

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

}