package com.kawaiichainwallet.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kawaiichainwallet.common.auth.JwtTokenService;
import com.kawaiichainwallet.common.auth.JwtValidationService;
import com.kawaiichainwallet.common.core.enums.ApiCode;
import com.kawaiichainwallet.common.core.exception.BusinessException;
import com.kawaiichainwallet.common.core.utils.TimeUtil;
import com.kawaiichainwallet.common.core.utils.ValidationUtil;
import com.kawaiichainwallet.user.converter.AuthConverter;
import com.kawaiichainwallet.user.dto.*;
import com.kawaiichainwallet.user.entity.User;
import com.kawaiichainwallet.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务
 * <p>
 * JWT使用说明：
 * - JwtTokenService: 生成Access Token和Refresh Token（登录、注册、刷新Token时使用）
 * - JwtValidationService: 仅用于验证Refresh Token（刷新Token接口使用）
 * <p>
 * 架构设计：
 * - Gateway验证所有外部请求的Access Token，并将用户信息注入到请求头
 * - User服务通过请求头获取用户信息，无需再次验证Access Token
 * - 只在刷新Token场景需要验证Refresh Token的有效性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final AuthConverter authConverter;
    private final OtpService otpService;
    private final JwtTokenService jwtTokenService;
    private final JwtValidationService jwtValidationService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;

    @Value("${app.auth.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.auth.account-lock-duration:30}")
    private int accountLockDurationMinutes;

    /**
     * 用户名/邮箱/手机号 + 密码登录
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, String clientIp, String userAgent) {
        // 验证请求参数
        validateLoginRequest(request);

        // 查找用户
        User user = userMapper.findByIdentifier(request.getIdentifier());
        if (user == null) {
            // 记录登录失败
            log.warn("Login failed: user not found - identifier: {}, IP: {}",
                    ValidationUtil.maskSensitiveInfo(request.getIdentifier()), clientIp);
            throw new BusinessException(ApiCode.INVALID_CREDENTIALS, "用户名或密码错误");
        }

        // 检查账户状态
        checkAccountStatus(user, clientIp, userAgent);

        // 验证密码
        boolean passwordValid = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!passwordValid) {
            // 增加登录失败次数
            handleLoginFailure(user, clientIp, userAgent, "密码错误");
            throw new BusinessException(ApiCode.INVALID_CREDENTIALS, "用户名或密码错误");
        }

        // 登录成功，处理后续逻辑
        return handleLoginSuccess(user, clientIp, userAgent, "PASSWORD_LOGIN");
    }

    /**
     * 手机验证码登录
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse loginWithOtp(OtpLoginRequest request, String clientIp, String userAgent) {
        // 验证请求参数
        validateOtpLoginRequest(request);

        // 验证OTP
        boolean otpValid = otpService.verifyOtp(request.getPhone(), "phone", "login", request.getOtpCode());
        if (!otpValid) {
            throw new BusinessException(ApiCode.OTP_INVALID, "验证码错误或已过期");
        }

        // 查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, request.getPhone())
                        .ne(User::getStatus, "deleted"));
        if (user == null) {
            throw new BusinessException(ApiCode.USER_NOT_FOUND, "用户不存在");
        }

        // 检查账户状态
        checkAccountStatus(user, clientIp, userAgent);

        // 登录成功
        return handleLoginSuccess(user, clientIp, userAgent, "OTP_LOGIN");
    }

    /**
     * 发送登录验证码
     */
    public void sendLoginOtp(String phone, String clientIp, String userAgent) {
        // 验证手机号格式
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new BusinessException(ApiCode.INVALID_PHONE_FORMAT);
        }

        // 检查用户是否存在
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, phone)
                        .ne(User::getStatus, "deleted"));
        if (user == null) {
            // 为了安全，不暴露用户是否存在，但记录日志
            // 仍然返回成功，不暴露用户是否存在
        } else {
            // 检查账户状态
            checkAccountStatus(user, clientIp, userAgent);

            // 发送验证码
            otpService.sendOtp(phone, "phone", "login");

            // 记录审计日志
        }

        log.info("发送登录验证码请求: phone={}, IP={}", ValidationUtil.maskPhone(phone), clientIp);
    }

    /**
     * 刷新Token
     */
    public LoginResponse refreshToken(RefreshTokenRequest request, String clientIp, String userAgent) {
        try {
            // 验证Refresh Token
            if (!jwtValidationService.validateRefreshToken(request.getRefreshToken())) {
                throw new BusinessException(ApiCode.INVALID_TOKEN, "Refresh Token无效");
            }

            // 从Token中提取用户信息
            Long userId = jwtValidationService.getUserIdFromToken(request.getRefreshToken());
            String username = jwtValidationService.getUsernameFromToken(request.getRefreshToken());

            // 验证用户是否仍然有效
            User user = userMapper.selectById(userId);
            if (user == null || !"active".equals(user.getStatus())) {
                throw new BusinessException(ApiCode.USER_NOT_FOUND, "用户不存在或已被禁用");
            }

            // 生成新的Token（roles使用用户的实际角色）
            String newAccessToken = jwtTokenService.generateAccessToken(userId, username, "USER");
            String newRefreshToken = jwtTokenService.generateRefreshToken(userId, username);

            // 记录审计日志

            // 使用MapStruct构建响应
            LoginResponse response = authConverter.createLoginResponse(userId, username);
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn((int) jwtTokenService.getAccessTokenExpiration());

            return response;
        } catch (Exception e) {
            throw new BusinessException(ApiCode.INVALID_TOKEN, "Token刷新失败");
        }
    }

    /**
     * 用户登出
     */
    public void logout(Long userId, String clientIp, String userAgent) {
        // 如果userId为null，说明Token无效或已过期，直接返回成功
        if (userId == null) {
            log.info("匿名用户登出: IP={}", clientIp);
            return;
        }

        // 记录登出审计日志
        // TODO: 将Token加入黑名单（需要Redis实现）
        log.info("用户登出成功: userId={}, IP={}", userId, clientIp);
    }

    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest request, String clientIp, String userAgent) {
        return userService.register(request, clientIp, userAgent);
    }

    /**
     * 发送注册验证码
     */
    public void sendRegisterOtp(String target, String type, String clientIp, String userAgent) {
        userService.sendRegisterOtp(target, type, clientIp, userAgent);
    }

    /**
     * 验证登录请求
     */
    private void validateLoginRequest(LoginRequest request) {
        if (request.getIdentifier() == null || request.getIdentifier().trim().isEmpty()) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "请输入用户名、邮箱或手机号");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "请输入密码");
        }
    }

    /**
     * 验证OTP登录请求
     */
    private void validateOtpLoginRequest(OtpLoginRequest request) {
        if (!ValidationUtil.isValidPhone(request.getPhone())) {
            throw new BusinessException(ApiCode.INVALID_PHONE_FORMAT);
        }

        if (request.getOtpCode() == null || request.getOtpCode().trim().isEmpty()) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "请输入验证码");
        }
    }

    /**
     * 检查账户状态
     */
    private void checkAccountStatus(User user, String clientIp, String userAgent) {
        // 检查用户状态
        if (!"active".equals(user.getStatus())) {
            String errorMsg = switch (user.getStatus()) {
                case "inactive" -> "账户未激活";
                case "suspended" -> "账户已被暂停";
                case "deleted" -> "账户已被删除";
                default -> "账户状态异常";
            };

            throw new BusinessException(ApiCode.ACCOUNT_DISABLED, errorMsg);
        }

        // 检查账户是否被锁定（使用 UTC 时间）
        LocalDateTime now = TimeUtil.nowUtc();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new BusinessException(ApiCode.ACCOUNT_LOCKED,
                    String.format("账户已被锁定，解锁时间: %s", user.getLockedUntil()));
        }

        // 如果锁定时间已过，解锁账户
        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(now)) {
            userMapper.unlockUser(user.getUserId());
            log.info("账户自动解锁: userId={}", user.getUserId());
        }
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(User user, String clientIp, String userAgent, String reason) {
        // 增加登录失败次数
        userMapper.incrementLoginAttempts(user.getUserId());

        int currentAttempts = user.getLoginAttempts() + 1;

        // 检查是否需要锁定账户（使用 UTC 时间）
        if (currentAttempts >= maxLoginAttempts) {
            LocalDateTime lockUntil = TimeUtil.nowUtc().plusMinutes(accountLockDurationMinutes);
            userMapper.lockUser(user.getUserId(), lockUntil);


            log.warn("账户被锁定: userId={}, attempts={}, lockUntil={}",
                    user.getUserId(), currentAttempts, lockUntil);
        } else {
        }
    }

    /**
     * 处理登录成功
     */
    private LoginResponse handleLoginSuccess(User user, String clientIp, String userAgent, String loginMethod) {
        // 重置登录失败次数
        userMapper.resetLoginAttempts(user.getUserId());

        // 更新最后登录信息（使用 UTC 时间）
        userMapper.updateLoginInfo(user.getUserId(), TimeUtil.nowUtc(), clientIp);

        // 生成JWT令牌
        String accessToken = jwtTokenService.generateAccessToken(user.getUserId(), user.getUsername(), "USER");
        String refreshToken = jwtTokenService.generateRefreshToken(user.getUserId(), user.getUsername());

        // 记录登录成功审计日志

        // 使用MapStruct构建响应，自动处理脱敏
        LoginResponse response = authConverter.userToLoginResponse(user);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn((int) jwtTokenService.getAccessTokenExpiration());

        log.info("用户登录成功: userId={}, username={}, method={}, IP={}",
                user.getUserId(), user.getUsername(), loginMethod, clientIp);

        return response;
    }

    /**
     * 验证OTP验证码
     *
     * @return 验证成功返回验证Token, 验证失败返回null
     */
    public String verifyOtp(String target, String type, String otpCode, String purpose, String clientIp) {
        log.info("验证OTP: target={}, type={}, purpose={}, IP={}", target, type, purpose, clientIp);

        // 验证参数
        if ("phone".equals(type)) {
            ValidationUtil.isValidPhone(target);
        } else if ("email".equals(type)) {
            ValidationUtil.isValidEmail(target);
        }

        // 调用OTP服务验证
        boolean isValid = otpService.verifyOtp(target, otpCode, type, clientIp);

        if (isValid) {
            log.info("OTP验证成功: target={}, purpose={}", target, purpose);

            // 生成验证Token
            String verificationToken = verificationTokenService.generateToken(target, type, purpose);
            return verificationToken;
        } else {
            log.warn("OTP验证失败: target={}, purpose={}, IP={}", target, purpose, clientIp);
            return null;
        }
    }
}