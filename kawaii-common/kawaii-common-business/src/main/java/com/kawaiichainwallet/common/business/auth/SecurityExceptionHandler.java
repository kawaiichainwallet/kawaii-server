package com.kawaiichainwallet.common.business.auth;

import com.kawaiichainwallet.common.enums.ApiCode;
import com.kawaiichainwallet.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Spring Security 相关异常处理器
 * 处理认证和授权相关的异常
 */
@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * 处理认证异常
     */
    @ExceptionHandler({BadCredentialsException.class, AuthenticationCredentialsNotFoundException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleAuthenticationException(Exception e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return R.error(ApiCode.UNAUTHORIZED, "认证失败");
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return R.error(ApiCode.FORBIDDEN, "权限不足");
    }

    /**
     * 处理JWT相关异常
     */
    @ExceptionHandler(org.springframework.security.oauth2.jwt.JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleJwtException(org.springframework.security.oauth2.jwt.JwtException e) {
        log.warn("JWT validation failed: {}", e.getMessage());
        return R.error(ApiCode.TOKEN_INVALID, "Token验证失败");
    }

    /**
     * 处理JWT解码异常
     */
    @ExceptionHandler(org.springframework.security.oauth2.jwt.JwtValidationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleJwtValidationException(org.springframework.security.oauth2.jwt.JwtValidationException e) {
        log.warn("JWT validation failed: {}", e.getMessage());
        return R.error(ApiCode.TOKEN_EXPIRED, "Token已过期或无效");
    }

    /**
     * 处理其他Spring Security异常
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleAuthenticationException(org.springframework.security.core.AuthenticationException e) {
        log.warn("Spring Security authentication failed: {}", e.getMessage());
        return R.error(ApiCode.UNAUTHORIZED, "认证失败: " + e.getMessage());
    }
}