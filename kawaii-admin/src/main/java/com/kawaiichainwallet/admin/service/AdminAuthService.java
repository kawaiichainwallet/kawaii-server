package com.kawaiichainwallet.admin.service;

import com.kawaiichainwallet.admin.dto.AdminLoginRequest;
import com.kawaiichainwallet.admin.dto.AdminLoginResponse;
import com.kawaiichainwallet.admin.entity.AdminRole;
import com.kawaiichainwallet.admin.entity.AdminUser;
import com.kawaiichainwallet.admin.mapper.AdminUserMapper;
import com.kawaiichainwallet.admin.mapper.AdminUserRoleMapper;
import com.kawaiichainwallet.common.auth.JwtTokenService;
import com.kawaiichainwallet.common.auth.JwtValidationService;
import com.kawaiichainwallet.common.auth.TokenBlacklistService;
import com.kawaiichainwallet.common.core.enums.ApiCode;
import com.kawaiichainwallet.common.core.exception.BusinessException;
import com.kawaiichainwallet.common.core.utils.TimeUtil;
import com.kawaiichainwallet.common.core.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员认证服务
 *
 * <p>JWT使用说明：
 * - JwtTokenService: 生成Access Token和Refresh Token
 * - JwtValidationService: 验证Token有效性
 * - TokenBlacklistService: 管理Token黑名单
 *
 * @author KawaiiChain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final JwtTokenService jwtTokenService;
    private final JwtValidationService jwtValidationService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.auth.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.auth.account-lock-duration:15}")
    private int accountLockDurationMinutes;

    /**
     * 管理员登录
     *
     * @param request 登录请求
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @return 登录响应
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminLoginResponse login(AdminLoginRequest request, String clientIp, String userAgent) {
        // 验证请求参数
        validateLoginRequest(request);

        // 查找管理员
        AdminUser admin = adminUserMapper.findByIdentifier(request.getIdentifier());
        if (admin == null) {
            log.warn("管理员登录失败: 用户不存在 - identifier: {}, IP: {}",
                    ValidationUtil.maskSensitiveInfo(request.getIdentifier()), clientIp);
            throw new BusinessException(ApiCode.INVALID_CREDENTIALS, "用户名或密码错误");
        }

        // 检查账户状态
        checkAccountStatus(admin, clientIp);

        // 验证密码
        boolean passwordValid = passwordEncoder.matches(request.getPassword(), admin.getPasswordHash());
        if (!passwordValid) {
            // 增加登录失败次数
            handleLoginFailure(admin, clientIp, "密码错误");
            throw new BusinessException(ApiCode.INVALID_CREDENTIALS, "用户名或密码错误");
        }

        // 登录成功，处理后续逻辑
        return handleLoginSuccess(admin, clientIp, userAgent);
    }

    /**
     * 管理员登出
     *
     * @param adminId 管理员ID
     * @param token Access Token
     * @param clientIp 客户端IP
     */
    public void logout(Long adminId, String token, String clientIp) {
        // 如果adminId为null，说明Token无效或已过期，直接返回成功
        if (adminId == null) {
            log.info("匿名管理员登出: IP={}", clientIp);
            return;
        }

        // 将Access Token加入黑名单
        if (token != null && !token.isEmpty()) {
            try {
                tokenBlacklistService.addToBlacklist(token);
                log.info("管理员Token已加入黑名单: adminId={}", adminId);
            } catch (Exception e) {
                log.error("将Token加入黑名单失败: adminId={}", adminId, e);
                // 不抛出异常，确保登出操作能够完成
            }
        }

        log.info("管理员登出成功: adminId={}, IP={}", adminId, clientIp);
    }

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新令牌
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    public AdminLoginResponse refreshToken(String refreshToken, String clientIp) {
        try {
            // 验证Refresh Token
            if (!jwtValidationService.validateRefreshToken(refreshToken)) {
                throw new BusinessException(ApiCode.INVALID_TOKEN, "Refresh Token无效");
            }

            // 从Token中提取用户信息
            Long adminId = jwtValidationService.getUserIdFromToken(refreshToken);
            String username = jwtValidationService.getUsernameFromToken(refreshToken);

            // 验证管理员是否仍然有效
            AdminUser admin = adminUserMapper.selectById(adminId);
            if (admin == null || !"active".equals(admin.getStatus())) {
                throw new BusinessException(ApiCode.USER_NOT_FOUND, "管理员不存在或已被禁用");
            }

            // 查询管理员角色
            List<AdminRole> roles = adminUserRoleMapper.findRolesByAdminId(adminId);
            String rolesStr = roles.stream()
                    .map(AdminRole::getRoleCode)
                    .collect(Collectors.joining(","));

            // 生成新的Token（指定用户类型为ADMIN）
            String newAccessToken = jwtTokenService.generateAccessToken(adminId, username, rolesStr, "ADMIN");
            String newRefreshToken = jwtTokenService.generateRefreshToken(adminId, username, "ADMIN");

            // 构建响应
            AdminLoginResponse response = buildLoginResponse(admin, roles);
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn((int) jwtTokenService.getAccessTokenExpiration());

            log.info("管理员Token刷新成功: adminId={}, IP={}", adminId, clientIp);

            return response;
        } catch (Exception e) {
            log.error("Token刷新失败: {}", e.getMessage());
            throw new BusinessException(ApiCode.INVALID_TOKEN, "Token刷新失败");
        }
    }

    /**
     * 验证登录请求
     */
    private void validateLoginRequest(AdminLoginRequest request) {
        if (request.getIdentifier() == null || request.getIdentifier().trim().isEmpty()) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "请输入用户名或邮箱");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "请输入密码");
        }
    }

    /**
     * 检查账户状态
     */
    private void checkAccountStatus(AdminUser admin, String clientIp) {
        // 检查管理员状态
        if (!"active".equals(admin.getStatus())) {
            String errorMsg = switch (admin.getStatus()) {
                case "inactive" -> "账户未激活";
                case "suspended" -> "账户已被暂停";
                default -> "账户状态异常";
            };

            log.warn("管理员账户状态异常: adminId={}, status={}, IP={}",
                    admin.getAdminId(), admin.getStatus(), clientIp);
            throw new BusinessException(ApiCode.ACCOUNT_DISABLED, errorMsg);
        }

        // 检查账户是否被锁定（使用 UTC 时间）
        LocalDateTime now = TimeUtil.nowUtc();
        if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(now)) {
            throw new BusinessException(ApiCode.ACCOUNT_LOCKED,
                    String.format("账户已被锁定，解锁时间: %s", admin.getLockedUntil()));
        }

        // 如果锁定时间已过，解锁账户
        if (admin.getLockedUntil() != null && admin.getLockedUntil().isBefore(now)) {
            adminUserMapper.unlockUser(admin.getAdminId());
            log.info("管理员账户自动解锁: adminId={}", admin.getAdminId());
        }
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(AdminUser admin, String clientIp, String reason) {
        // 增加登录失败次数
        adminUserMapper.incrementLoginAttempts(admin.getAdminId());

        int currentAttempts = admin.getLoginAttempts() + 1;

        // 检查是否需要锁定账户（使用 UTC 时间）
        if (currentAttempts >= maxLoginAttempts) {
            LocalDateTime lockUntil = TimeUtil.nowUtc().plusMinutes(accountLockDurationMinutes);
            adminUserMapper.lockUser(admin.getAdminId(), lockUntil);

            log.warn("管理员账户被锁定: adminId={}, attempts={}, lockUntil={}, reason={}, IP={}",
                    admin.getAdminId(), currentAttempts, lockUntil, reason, clientIp);

            throw new BusinessException(ApiCode.ACCOUNT_LOCKED,
                    String.format("登录失败次数过多，账户已被锁定%d分钟", accountLockDurationMinutes));
        } else {
            log.warn("管理员登录失败: adminId={}, attempts={}/{}, reason={}, IP={}",
                    admin.getAdminId(), currentAttempts, maxLoginAttempts, reason, clientIp);
        }
    }

    /**
     * 处理登录成功
     */
    private AdminLoginResponse handleLoginSuccess(AdminUser admin, String clientIp, String userAgent) {
        // 重置登录失败次数
        adminUserMapper.resetLoginAttempts(admin.getAdminId());

        // 更新最后登录信息（使用 UTC 时间）
        adminUserMapper.updateLoginInfo(admin.getAdminId(), TimeUtil.nowUtc(), clientIp);

        // 查询管理员角色
        List<AdminRole> roles = adminUserRoleMapper.findRolesByAdminId(admin.getAdminId());

        // 生成角色字符串（用于JWT）
        String rolesStr = roles.stream()
                .map(AdminRole::getRoleCode)
                .collect(Collectors.joining(","));

        // 生成JWT令牌（指定用户类型为ADMIN）
        String accessToken = jwtTokenService.generateAccessToken(
                admin.getAdminId(),
                admin.getUsername(),
                rolesStr.isEmpty() ? "ADMIN" : rolesStr,
                "ADMIN"  // 用户类型
        );
        String refreshToken = jwtTokenService.generateRefreshToken(
                admin.getAdminId(),
                admin.getUsername(),
                "ADMIN"  // 用户类型
        );

        // 构建响应
        AdminLoginResponse response = buildLoginResponse(admin, roles);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn((int) jwtTokenService.getAccessTokenExpiration());

        log.info("管理员登录成功: adminId={}, username={}, roles={}, IP={}",
                admin.getAdminId(), admin.getUsername(), rolesStr, clientIp);

        return response;
    }

    /**
     * 构建登录响应
     */
    private AdminLoginResponse buildLoginResponse(AdminUser admin, List<AdminRole> roles) {
        AdminLoginResponse response = new AdminLoginResponse();
        response.setAdminId(admin.getAdminId());
        response.setUsername(admin.getUsername());
        response.setEmail(ValidationUtil.maskEmail(admin.getEmail()));
        response.setRealName(admin.getRealName());
        response.setIsSuperAdmin(admin.getIsSuperAdmin());

        // 设置角色列表
        List<String> roleCodes = roles.stream()
                .map(AdminRole::getRoleCode)
                .collect(Collectors.toList());
        response.setRoles(roleCodes);

        // 收集所有权限（角色权限 + 用户额外权限）
        List<String> allPermissions = roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toList());

        // 添加用户的额外权限
        if (admin.getPermissions() != null && !admin.getPermissions().isEmpty()) {
            allPermissions.addAll(admin.getPermissions());
        }

        // 如果是超级管理员，拥有所有权限
        if (Boolean.TRUE.equals(admin.getIsSuperAdmin())) {
            allPermissions.clear();
            allPermissions.add("*");
        }

        response.setPermissions(allPermissions.stream().distinct().collect(Collectors.toList()));

        return response;
    }
}
