package com.kawaiichainwallet.user.service;

import com.kawaiichainwallet.common.enums.ApiCode;
import com.kawaiichainwallet.common.exception.BusinessException;
import com.kawaiichainwallet.common.utils.CryptoUtil;
import com.kawaiichainwallet.common.utils.JwtUtil;
import com.kawaiichainwallet.common.utils.ValidationUtil;
import com.kawaiichainwallet.user.dto.RegisterResponse;
import com.kawaiichainwallet.user.dto.SendOtpRequest;
import com.kawaiichainwallet.user.dto.UserInfoResponse;
import com.kawaiichainwallet.user.dto.VerifyOtpRequest;
import com.kawaiichainwallet.user.entity.User;
import com.kawaiichainwallet.user.entity.UserProfile;
import com.kawaiichainwallet.user.mapper.UserMapper;
import com.kawaiichainwallet.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    /**
     * 发送注册验证码
     */
    public void sendRegisterOtp(SendOtpRequest request) {
        // 验证请求参数
        validateSendOtpRequest(request);

        // 检查用户是否已存在
        checkUserNotExists(request.getTarget(), request.getType());

        // 发送验证码
        otpService.sendOtp(request.getTarget(), request.getType(), request.getPurpose());

        // 记录审计日志
        auditService.logAction(null, "SEND_OTP", "otp", null,
                String.format("target=%s, type=%s, purpose=%s",
                        ValidationUtil.maskPhone(request.getTarget()),
                        request.getType(), request.getPurpose()));
    }

    /**
     * 验证OTP并完成注册
     */
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse registerWithOtp(VerifyOtpRequest request) {
        // 验证请求参数
        validateVerifyOtpRequest(request);

        // 验证OTP
        boolean otpValid = otpService.verifyOtp(request.getTarget(), request.getType(), "register", request.getOtpCode());
        if (!otpValid) {
            auditService.logAction(null, "REGISTER_FAILED", "user", null,
                    "OTP验证失败: " + ValidationUtil.maskPhone(request.getTarget()));
            throw new BusinessException(ApiCode.OTP_INVALID, "验证码错误");
        }

        // 再次检查用户是否已存在（防止并发注册）
        checkUserNotExists(request.getTarget(), request.getType());
        checkUsernameNotExists(request.getUsername());

        // 创建用户
        User user = createUser(request);
        userMapper.insertUser(user);

        // 创建用户资料
        UserProfile userProfile = createUserProfile(user.getUserId(), request.getTarget(), request.getType());
        userProfileMapper.insertUserProfile(userProfile);

        // 生成JWT令牌
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getUsername());

        // 记录审计日志
        auditService.logAction(user.getUserId(), "REGISTER_SUCCESS", "user", user.getUserId(),
                String.format("用户注册成功: username=%s, %s=%s",
                        user.getUsername(), request.getType(), ValidationUtil.maskPhone(request.getTarget())));

        // 构建响应
        RegisterResponse response = new RegisterResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(900L); // 15分钟

        if ("phone".equals(request.getType())) {
            response.setPhone(ValidationUtil.maskPhone(request.getTarget()));
        } else {
            response.setEmail(ValidationUtil.maskEmail(request.getTarget()));
        }

        log.info("用户注册成功: userId={}, username={}", user.getUserId(), user.getUsername());
        return response;
    }

    /**
     * 获取用户信息
     */
    public UserInfoResponse getUserInfo(String userId) {
        User user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new BusinessException(ApiCode.USER_NOT_FOUND);
        }

        UserProfile userProfile = userProfileMapper.findByUserId(userId);

        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);

        if (userProfile != null) {
            response.setDisplayName(userProfile.getDisplayName());
            response.setAvatarUrl(userProfile.getAvatarUrl());
            response.setLanguage(userProfile.getLanguage());
            response.setTimezone(userProfile.getTimezone());
            response.setCurrency(userProfile.getCurrency());
        }

        // 脱敏处理
        response.setPhone(ValidationUtil.maskPhone(user.getPhone()));
        response.setEmail(ValidationUtil.maskEmail(user.getEmail()));

        return response;
    }

    /**
     * 验证发送OTP请求
     */
    private void validateSendOtpRequest(SendOtpRequest request) {
        if ("phone".equals(request.getType()) && !ValidationUtil.isValidPhone(request.getTarget())) {
            throw new BusinessException(ApiCode.INVALID_PHONE_FORMAT);
        }

        if ("email".equals(request.getType()) && !ValidationUtil.isValidEmail(request.getTarget())) {
            throw new BusinessException(ApiCode.INVALID_EMAIL_FORMAT);
        }
    }

    /**
     * 验证验证OTP请求
     */
    private void validateVerifyOtpRequest(VerifyOtpRequest request) {
        // 验证用户名
        if (!ValidationUtil.isValidUsername(request.getUsername())) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, ValidationUtil.getUsernameValidationMessage());
        }

        // 验证密码
        if (!ValidationUtil.isValidPassword(request.getPassword())) {
            throw new BusinessException(ApiCode.WEAK_PASSWORD, ValidationUtil.getPasswordValidationMessage());
        }

        // 验证确认密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "两次输入的密码不一致");
        }

        // 验证用户协议
        if (request.getAgreeToTerms() == null || !request.getAgreeToTerms()) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "请同意用户服务协议");
        }
    }

    /**
     * 检查用户不存在
     */
    private void checkUserNotExists(String target, String type) {
        if ("phone".equals(type) && userMapper.existsByPhone(target)) {
            throw new BusinessException(ApiCode.PHONE_ALREADY_EXISTS);
        }

        if ("email".equals(type) && userMapper.existsByEmail(target)) {
            throw new BusinessException(ApiCode.EMAIL_ALREADY_EXISTS);
        }
    }

    /**
     * 检查用户名不存在
     */
    private void checkUsernameNotExists(String username) {
        if (userMapper.existsByUsername(username)) {
            throw new BusinessException(ApiCode.USER_ALREADY_EXISTS, "用户名已被使用");
        }
    }

    /**
     * 创建用户
     */
    private User createUser(VerifyOtpRequest request) {
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());

        // 设置手机号或邮箱
        if ("phone".equals(request.getType())) {
            user.setPhone(request.getTarget());
            user.setPhoneVerified(true);  // 通过OTP验证，标记为已验证
            user.setEmailVerified(false);
        } else {
            user.setEmail(request.getTarget());
            user.setEmailVerified(true);  // 通过OTP验证，标记为已验证
            user.setPhoneVerified(false);
        }

        // 加密密码
        String salt = CryptoUtil.generateSalt();
        String passwordHash = CryptoUtil.encryptPassword(request.getPassword(), salt);
        user.setPasswordHash(passwordHash);
        user.setSalt(salt);

        // 设置默认值
        user.setStatus("active");
        user.setTwoFactorEnabled(false);
        user.setLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    /**
     * 创建用户资料
     */
    private UserProfile createUserProfile(String userId, String target, String type) {
        UserProfile userProfile = new UserProfile();
        userProfile.setProfileId(UUID.randomUUID().toString());
        userProfile.setUserId(userId);

        // 设置默认显示名称
        if ("phone".equals(type)) {
            userProfile.setDisplayName("用户" + target.substring(target.length() - 4));
        } else {
            String emailPrefix = target.substring(0, target.indexOf("@"));
            userProfile.setDisplayName(emailPrefix);
        }

        // 设置默认偏好
        userProfile.setLanguage("zh-CN");
        userProfile.setTimezone("Asia/Shanghai");
        userProfile.setCurrency("CNY");
        userProfile.setNotificationsEnabled(true);

        userProfile.setCreatedAt(LocalDateTime.now());
        userProfile.setUpdatedAt(LocalDateTime.now());

        return userProfile;
    }
}