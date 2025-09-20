package com.kawaiichainwallet.gateway.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * JWT验证服务 - 使用Spring Security标准组件
 */
@Slf4j
@Service
public class JwtValidationService {

    private final JwtDecoder jwtDecoder;

    public JwtValidationService(@Value("${app.jwt.secret}") String jwtSecret) {
        // 使用与user服务相同的密钥创建JWT解码器
        SecretKeySpec secretKey = new SecretKeySpec(
            jwtSecret.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );

        this.jwtDecoder = NimbusJwtDecoder
            .withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }

    /**
     * 验证JWT Token
     */
    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从JWT Token中提取用户ID
     */
    public String getUserIdFromToken(String token) {
        try {
            var jwt = jwtDecoder.decode(token);
            return jwt.getClaimAsString("userId");
        } catch (JwtException e) {
            log.debug("Failed to extract userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT Token中提取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            var jwt = jwtDecoder.decode(token);
            return jwt.getClaimAsString("username");
        } catch (JwtException e) {
            log.debug("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT Token中提取Token类型
     */
    public String getTokenTypeFromToken(String token) {
        try {
            var jwt = jwtDecoder.decode(token);
            return jwt.getClaimAsString("tokenType");
        } catch (JwtException e) {
            log.debug("Failed to extract tokenType from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证访问令牌
     */
    public boolean validateAccessToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        String tokenType = getTokenTypeFromToken(token);
        return "access".equals(tokenType);
    }

    /**
     * 从Authorization header中提取JWT令牌
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}