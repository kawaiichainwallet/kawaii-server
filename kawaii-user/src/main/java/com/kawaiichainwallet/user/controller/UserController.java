package com.kawaiichainwallet.user.controller;

import com.kawaiichainwallet.common.enums.ApiCode;
import com.kawaiichainwallet.common.response.R;
import com.kawaiichainwallet.common.utils.ValidationUtil;
import com.kawaiichainwallet.user.context.UserContextHolder;
import com.kawaiichainwallet.user.dto.UpdateUserInfoRequest;
import com.kawaiichainwallet.user.dto.UserInfoResponse;
import com.kawaiichainwallet.user.entity.User;
import com.kawaiichainwallet.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器 - 专注用户信息管理
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public R<UserInfoResponse> getCurrentUserInfo() {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return R.error(ApiCode.UNAUTHORIZED);
        }

        log.info("获取用户信息请求: userId={}", userId);
        UserInfoResponse response = userService.getUserInfo(userId);
        return R.success(response);
    }

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/{userId}")
    @Operation(summary = "根据ID获取用户信息", description = "管理员或授权用户获取指定用户信息")
    public R<UserInfoResponse> getUserInfo(@PathVariable String userId) {
        log.info("获取指定用户信息请求: userId={}", userId);
        UserInfoResponse response = userService.getUserInfo(userId);
        return R.success(response);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    public R<UserInfoResponse> updateCurrentUserInfo(@Valid @RequestBody UpdateUserInfoRequest request) {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return R.error(ApiCode.UNAUTHORIZED);
        }

        log.info("更新用户信息请求: userId={}", userId);
        UserInfoResponse response = userService.updateUserInfo(userId, request);
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
    public R<List<UserInfoResponse>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        log.info("获取用户列表请求: page={}, size={}, status={}", page, size, status);
        List<UserInfoResponse> users = userService.getUserList(page, size, status);
        return R.success(users);
    }

}