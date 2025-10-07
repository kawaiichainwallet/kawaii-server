package com.kawaiichainwallet.user.service;

import com.kawaiichainwallet.common.core.enums.ApiCode;
import com.kawaiichainwallet.common.core.exception.BusinessException;
import com.kawaiichainwallet.common.core.utils.ValidationUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.kawaiichainwallet.user.dto.UserDetailsDto;
import com.kawaiichainwallet.user.dto.UpdateUserInfoRequest;
import com.kawaiichainwallet.user.dto.RegisterRequest;
import com.kawaiichainwallet.user.dto.RegisterResponse;
import com.kawaiichainwallet.user.entity.User;
import com.kawaiichainwallet.user.entity.UserProfile;
import com.kawaiichainwallet.user.mapper.UserMapper;
import com.kawaiichainwallet.user.mapper.UserProfileMapper;
import com.kawaiichainwallet.user.converter.UserConverter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 用户服务 - 专注用户信息管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserConverter userConverter;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final VerificationTokenService verificationTokenService;
    private final DistributedIdService distributedIdService;

    /**
     * 根据用户ID获取用户基本信息
     */
    public User getUserById(long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ApiCode.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 根据用户名查询用户
     */
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    /**
     * 根据邮箱查询用户
     */
    public User getUserByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    /**
     * 根据手机号查询用户
     */
    public User getUserByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean isUsernameExists(String username) {
        return userMapper.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean isEmailExists(String email) {
        return userMapper.existsByEmail(email);
    }

    /**
     * 检查手机号是否存在
     */
    public boolean isPhoneExists(String phone) {
        return userMapper.existsByPhone(phone);
    }

    /**
     * 获取用户详细信息（包含用户资料）
     */
    public UserDetailsDto getUserInfo(long userId) {
        User user = getUserById(userId);

        UserProfile userProfile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId));

        // 使用MapStruct进行对象转换和脱敏处理
        UserDetailsDto response;
        if (userProfile != null) {
            response = userConverter.userAndProfileToUserDetailsDto(user, userProfile);
        } else {
            response = userConverter.userToUserDetailsDto(user);
        }

        return response;
    }

    /**
     * 更新用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDetailsDto updateUserInfo(long userId, UpdateUserInfoRequest request) {
        User user = getUserById(userId);

        // 更新用户基本信息
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            // 检查新用户名是否已存在
            if (isUsernameExists(request.getUsername())) {
                throw new BusinessException(ApiCode.USER_ALREADY_EXISTS, "用户名已被使用");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // 验证邮箱格式
            if (!ValidationUtil.isValidEmail(request.getEmail())) {
                throw new BusinessException(ApiCode.INVALID_EMAIL_FORMAT);
            }
            // 检查新邮箱是否已存在
            if (isEmailExists(request.getEmail())) {
                throw new BusinessException(ApiCode.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            // 验证手机号格式
            if (!ValidationUtil.isValidPhone(request.getPhone())) {
                throw new BusinessException(ApiCode.INVALID_PHONE_FORMAT);
            }
            // 检查新手机号是否已存在
            if (isPhoneExists(request.getPhone())) {
                throw new BusinessException(ApiCode.PHONE_ALREADY_EXISTS);
            }
            user.setPhone(request.getPhone());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 更新用户资料
        updateUserProfile(userId, request);

        log.info("用户信息更新成功: userId={}, username={}", userId, user.getUsername());
        return getUserInfo(userId);
    }

    /**
     * 获取用户列表（管理员功能）
     */
    public List<UserDetailsDto> getUserList(int page, int size, String status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .ne(User::getStatus, "deleted");

        if (status != null && !status.isEmpty()) {
            wrapper.eq(User::getStatus, status);
        }

        List<User> users = userMapper.selectList(wrapper);
        return users.stream()
                .map(userConverter::userToUserDetailsDto)
                .toList();
    }

    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest request, String clientIp, String userAgent) {
        // 1. 验证请求参数
        validateRegisterRequest(request);

        // 2. 验证Token (替代OTP验证)
        boolean tokenValid = verificationTokenService.verifyToken(
                request.getVerificationToken(),
                request.getTarget(),
                "register"
        );
        if (!tokenValid) {
            throw new BusinessException(ApiCode.INVALID_TOKEN, "验证Token无效或已过期");
        }

        // 3. 使用Token(防止重复使用)
        verificationTokenService.consumeToken(request.getVerificationToken());

        // 3. 检查用户名是否已存在
        if (isUsernameExists(request.getUsername())) {
            throw new BusinessException(ApiCode.USER_ALREADY_EXISTS, "用户名已被使用");
        }

        // 4. 检查邮箱或手机号是否已存在
        if ("email".equals(request.getType())) {
            if (isEmailExists(request.getTarget())) {
                throw new BusinessException(ApiCode.EMAIL_ALREADY_EXISTS, "邮箱已被注册");
            }
        } else if ("phone".equals(request.getType())) {
            if (isPhoneExists(request.getTarget())) {
                throw new BusinessException(ApiCode.PHONE_ALREADY_EXISTS, "手机号已被注册");
            }
        }

        // 5. 创建用户
        User user = createUser(request, clientIp);
        userMapper.insert(user);

        // 6. 创建用户资料
        createInitialUserProfile(user.getUserId());

        // 7. 记录注册成功审计日志

        log.info("用户注册成功: userId={}, username={}, type={}, IP={}",
                user.getUserId(), user.getUsername(), request.getType(), clientIp);

        // 8. 生成JWT令牌
        String accessToken = jwtTokenService.generateAccessToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtTokenService.generateRefreshToken(user.getUserId(), user.getUsername());

        // 9. 使用MapStruct转换响应对象并设置令牌
        RegisterResponse response = userConverter.userToRegisterResponse(user);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn((int) jwtTokenService.getAccessTokenExpiration());

        return response;
    }

    /**
     * 发送注册验证码
     */
    public void sendRegisterOtp(String target, String type, String clientIp, String userAgent) {
        // 验证目标格式
        if ("email".equals(type)) {
            if (!ValidationUtil.isValidEmail(target)) {
                throw new BusinessException(ApiCode.INVALID_EMAIL_FORMAT);
            }
        } else if ("phone".equals(type)) {
            if (!ValidationUtil.isValidPhone(target)) {
                throw new BusinessException(ApiCode.INVALID_PHONE_FORMAT);
            }
        } else {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "类型只能是phone或email");
        }

        // 检查是否已注册
        boolean exists = false;
        if ("email".equals(type)) {
            exists = isEmailExists(target);
        } else if ("phone".equals(type)) {
            exists = isPhoneExists(target);
        }

        if (exists) {
            throw new BusinessException(ApiCode.USER_ALREADY_EXISTS,
                    "email".equals(type) ? "邮箱已被注册" : "手机号已被注册");
        }

        // 发送验证码
        otpService.sendOtp(target, type, "register");

        // 记录审计日志

        log.info("发送注册验证码请求: target={}, type={}, IP={}",
                ValidationUtil.maskSensitiveInfo(target), type, clientIp);
    }

    /**
     * 验证注册请求
     */
    private void validateRegisterRequest(RegisterRequest request) {
        // 验证密码和确认密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "密码和确认密码不一致");
        }

        // 验证用户是否同意条款
        if (!Boolean.TRUE.equals(request.getAgreeToTerms())) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "请同意用户服务协议");
        }

        // 验证目标格式
        if ("email".equals(request.getType())) {
            if (!ValidationUtil.isValidEmail(request.getTarget())) {
                throw new BusinessException(ApiCode.INVALID_EMAIL_FORMAT);
            }
        } else if ("phone".equals(request.getType())) {
            if (!ValidationUtil.isValidPhone(request.getTarget())) {
                throw new BusinessException(ApiCode.INVALID_PHONE_FORMAT);
            }
        }

        // 验证用户名格式
        if (!ValidationUtil.isValidUsername(request.getUsername())) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "用户名格式不正确");
        }

        // 验证密码强度
        if (!ValidationUtil.isValidPassword(request.getPassword())) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "密码强度不足");
        }
    }

    /**
     * 创建用户对象
     */
    private User createUser(RegisterRequest request, String clientIp) {
        User user = new User();

        // 使用Leaf分布式ID生成器生成用户ID
        Long userId = distributedIdService.generateSegmentId("user-id").getId();
        user.setUserId(userId);
        user.setUsername(request.getUsername());

        // 根据注册类型设置邮箱或手机号
        if ("email".equals(request.getType())) {
            user.setEmail(request.getTarget());
            user.setEmailVerified(true); // OTP验证通过即认为已验证
        } else if ("phone".equals(request.getType())) {
            user.setPhone(request.getTarget());
            user.setPhoneVerified(true); // OTP验证通过即认为已验证
        }

        // 使用Spring Security的BCryptPasswordEncoder加密密码
        String passwordHash = passwordEncoder.encode(request.getPassword());
        user.setPasswordHash(passwordHash);

        // 设置默认状态
        user.setStatus("active");
        user.setTwoFactorEnabled(false);
        user.setLoginAttempts(0);
        user.setLastLoginIp(clientIp);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    /**
     * 创建初始用户资料
     */
    private void createInitialUserProfile(long userId) {
        UserProfile userProfile = new UserProfile();

        // 使用Leaf分布式ID生成器生成资料ID
        Long profileId = distributedIdService.generateSegmentId("user-id").getId();
        userProfile.setProfileId(profileId);
        userProfile.setUserId(userId);
        userProfile.setLanguage("en");
        userProfile.setTimezone("UTC");
        userProfile.setCurrency("USD");
        userProfile.setNotificationsEnabled(true);
        userProfile.setCreatedAt(LocalDateTime.now());
        userProfile.setUpdatedAt(LocalDateTime.now());

        userProfileMapper.insert(userProfile);
    }

    /**
     * 更新用户资料
     */
    private void updateUserProfile(long userId, UpdateUserInfoRequest request) {
        UserProfile userProfile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId));

        if (userProfile == null) {
            // 如果用户资料不存在，创建一个新的
            userProfile = new UserProfile();
            userProfile.setUserId(userId);
            userProfile.setCreatedAt(LocalDateTime.now());
        }

        // 更新用户资料字段
        if (request.getDisplayName() != null) {
            userProfile.setDisplayName(request.getDisplayName());
        }
        if (request.getAvatar() != null) {
            userProfile.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            userProfile.setBio(request.getBio());
        }
        if (request.getLanguage() != null) {
            userProfile.setLanguage(request.getLanguage());
        }
        if (request.getTimezone() != null) {
            userProfile.setTimezone(request.getTimezone());
        }
        if (request.getCurrency() != null) {
            userProfile.setCurrency(request.getCurrency());
        }
        if (request.getNotificationsEnabled() != null) {
            userProfile.setNotificationsEnabled(request.getNotificationsEnabled());
        }

        userProfile.setUpdatedAt(LocalDateTime.now());

        if (userProfile.getProfileId() == null) {
            userProfileMapper.insert(userProfile);
        } else {
            userProfileMapper.updateById(userProfile);
        }
    }
}