package com.kawaiichainwallet.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kawaiichainwallet.user.dto.LoginRequest;
import com.kawaiichainwallet.user.dto.LoginResponse;
import com.kawaiichainwallet.user.dto.OtpLoginRequest;
import com.kawaiichainwallet.user.dto.RefreshTokenRequest;
import com.kawaiichainwallet.user.dto.TokenValidationResponse;
import com.kawaiichainwallet.user.dto.RegisterRequest;
import com.kawaiichainwallet.user.dto.RegisterResponse;
import com.kawaiichainwallet.user.entity.User;
import com.kawaiichainwallet.user.mapper.UserMapper;
import com.kawaiichainwallet.user.converter.AuthConverter;
import com.kawaiichainwallet.common.enums.ApiCode;
import com.kawaiichainwallet.common.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.kawaiichainwallet.common.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final AuthConverter authConverter;
    private final OtpService otpService;
    private final JwtTokenService jwtTokenService;
    private final AuditService auditService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

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
            auditService.logAction(null, "LOGIN_FAILED", "user", null,
                    String.format("用户不存在: %s, IP: %s",
                            ValidationUtil.maskSensitiveInfo(request.getIdentifier()), clientIp),
                    clientIp, userAgent, false, "用户不存在");
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
            auditService.logAction(null, "LOGIN_FAILED", "user", null,
                    String.format("OTP验证失败: %s, IP: %s",
                            ValidationUtil.maskPhone(request.getPhone()), clientIp),
                    clientIp, userAgent, false, "验证码错误");
            throw new BusinessException(ApiCode.OTP_INVALID, "验证码错误或已过期");
        }

        // 查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, request.getPhone())
                        .ne(User::getStatus, "deleted"));
        if (user == null) {
            auditService.logAction(null, "LOGIN_FAILED", "user", null,
                    String.format("用户不存在: %s, IP: %s",
                            ValidationUtil.maskPhone(request.getPhone()), clientIp),
                    clientIp, userAgent, false, "用户不存在");
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
            auditService.logAction(null, "SEND_LOGIN_OTP_FAILED", "otp", null,
                    String.format("用户不存在: %s, IP: %s", ValidationUtil.maskPhone(phone), clientIp),
                    clientIp, userAgent, false, "用户不存在");
            // 仍然返回成功，不暴露用户是否存在
        } else {
            // 检查账户状态
            checkAccountStatus(user, clientIp, userAgent);

            // 发送验证码
            otpService.sendOtp(phone, "phone", "login");

            // 记录审计日志
            auditService.logAction(user.getUserId(), "SEND_LOGIN_OTP", "otp", null,
                    String.format("发送登录验证码: %s, IP: %s", ValidationUtil.maskPhone(phone), clientIp),
                    clientIp, userAgent, true, null);
        }

        log.info("发送登录验证码请求: phone={}, IP={}", ValidationUtil.maskPhone(phone), clientIp);
    }

    /**
     * 刷新Token
     */
    public LoginResponse refreshToken(RefreshTokenRequest request, String clientIp, String userAgent) {
        try {
            // 验证Refresh Token
            if (!jwtTokenService.validateRefreshToken(request.getRefreshToken())) {
                throw new BusinessException(ApiCode.INVALID_TOKEN, "Refresh Token无效");
            }

            // 从Token中提取用户信息
            String userId = jwtTokenService.extractUserIdFromToken(request.getRefreshToken());
            String username = jwtTokenService.extractUsernameFromToken(request.getRefreshToken());

            // 验证用户是否仍然有效
            User user = userMapper.selectById(userId);
            if (user == null || !"active".equals(user.getStatus())) {
                throw new BusinessException(ApiCode.USER_NOT_FOUND, "用户不存在或已被禁用");
            }

            // 生成新的Token
            String newAccessToken = jwtTokenService.generateAccessToken(userId, username);
            String newRefreshToken = jwtTokenService.generateRefreshToken(userId, username);

            // 记录审计日志
            auditService.logAction(userId, "REFRESH_TOKEN", "auth", null,
                    "刷新Token成功", clientIp, userAgent, true, null);

            // 使用MapStruct构建响应
            LoginResponse response = authConverter.createLoginResponse(userId, username);
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn((int) jwtTokenService.getAccessTokenExpiration());

            return response;
        } catch (Exception e) {
            auditService.logAction(null, "REFRESH_TOKEN_FAILED", "auth", null,
                    "刷新Token失败: " + e.getMessage(), clientIp, userAgent, false, e.getMessage());
            throw new BusinessException(ApiCode.INVALID_TOKEN, "Token刷新失败");
        }
    }

    /**
     * 用户登出
     */
    public void logout(String userId, String clientIp, String userAgent) {
        // 记录登出审计日志
        auditService.logAction(userId, "LOGOUT", "auth", userId,
                "用户登出", clientIp, userAgent, true, null);

        // TODO: 将Token加入黑名单（需要Redis实现）
        log.info("用户登出成功: userId={}, IP={}", userId, clientIp);
    }

    /**
     * 验证Token
     */
    public TokenValidationResponse validateToken(String token) {
        TokenValidationResponse response = new TokenValidationResponse();

        try {
            if (token == null || token.isEmpty()) {
                response.setValid(false);
                response.setErrorMessage("Token为空");
                return response;
            }

            boolean isValid = jwtTokenService.validateAccessToken(token);
            response.setValid(isValid);

            if (isValid) {
                String userId = jwtTokenService.extractUserIdFromToken(token);
                String username = jwtTokenService.extractUsernameFromToken(token);

                response.setUserId(userId);
                response.setUsername(username);
                response.setTokenType("Bearer");

                // 提取过期时间
                Map<String, Object> claims = jwtTokenService.extractAllClaims(token);
                if (claims != null && claims.containsKey("exp")) {
                    response.setExpiresAt((Long) claims.get("exp"));
                }
            } else {
                response.setErrorMessage("Token无效或已过期");
            }
        } catch (Exception e) {
            response.setValid(false);
            response.setErrorMessage("Token验证失败: " + e.getMessage());
            log.debug("Token验证失败", e);
        }

        return response;
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

            auditService.logAction(user.getUserId(), "LOGIN_FAILED", "user", user.getUserId(),
                    String.format("账户状态异常: %s, IP: %s", user.getStatus(), clientIp),
                    clientIp, userAgent, false, errorMsg);
            throw new BusinessException(ApiCode.ACCOUNT_DISABLED, errorMsg);
        }

        // 检查账户是否被锁定
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            auditService.logAction(user.getUserId(), "LOGIN_FAILED", "user", user.getUserId(),
                    String.format("账户已锁定: %s, IP: %s", user.getLockedUntil(), clientIp),
                    clientIp, userAgent, false, "账户已锁定");
            throw new BusinessException(ApiCode.ACCOUNT_LOCKED,
                    String.format("账户已被锁定，解锁时间: %s", user.getLockedUntil()));
        }

        // 如果锁定时间已过，解锁账户
        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(LocalDateTime.now())) {
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

        // 检查是否需要锁定账户
        if (currentAttempts >= maxLoginAttempts) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(accountLockDurationMinutes);
            userMapper.lockUser(user.getUserId(), lockUntil);

            auditService.logAction(user.getUserId(), "ACCOUNT_LOCKED", "user", user.getUserId(),
                    String.format("账户因登录失败次数过多被锁定: %d次, IP: %s", currentAttempts, clientIp),
                    clientIp, userAgent, true, null);

            log.warn("账户被锁定: userId={}, attempts={}, lockUntil={}",
                    user.getUserId(), currentAttempts, lockUntil);
        } else {
            auditService.logAction(user.getUserId(), "LOGIN_FAILED", "user", user.getUserId(),
                    String.format("登录失败: %s, 尝试次数: %d/%d, IP: %s",
                            reason, currentAttempts, maxLoginAttempts, clientIp),
                    clientIp, userAgent, false, reason);
        }
    }

    /**
     * 处理登录成功
     */
    private LoginResponse handleLoginSuccess(User user, String clientIp, String userAgent, String loginMethod) {
        // 重置登录失败次数
        userMapper.resetLoginAttempts(user.getUserId());

        // 更新最后登录信息
        userMapper.updateLoginInfo(user.getUserId(), LocalDateTime.now(), clientIp);

        // 生成JWT令牌
        String accessToken = jwtTokenService.generateAccessToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtTokenService.generateRefreshToken(user.getUserId(), user.getUsername());

        // 记录登录成功审计日志
        auditService.logAction(user.getUserId(), "LOGIN_SUCCESS", "user", user.getUserId(),
                String.format("登录成功: %s, IP: %s", loginMethod, clientIp),
                clientIp, userAgent, true, null);

        // 使用MapStruct构建响应，自动处理脱敏
        LoginResponse response = authConverter.userToLoginResponse(user);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn((int) jwtTokenService.getAccessTokenExpiration());

        log.info("用户登录成功: userId={}, username={}, method={}, IP={}",
                user.getUserId(), user.getUsername(), loginMethod, clientIp);

        return response;
    }
}