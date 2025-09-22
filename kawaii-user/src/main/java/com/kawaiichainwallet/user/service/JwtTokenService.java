package com.kawaiichainwallet.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * JWT Token服务
 * 使用Spring Security OAuth2官方JWT支持
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${app.security.jwt.issuer:kawaii-wallet-auth}")
    private String issuer;

    @Value("${app.security.jwt.access-token-expiration:900}") // 15分钟
    private long accessTokenExpiration;

    @Value("${app.security.jwt.refresh-token-expiration:2592000}") // 30天
    private long refreshTokenExpiration;

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(String userId, String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(userId)
                .claim("username", username)
                .claim("type", "access")
                .claim("roles", "USER")
                .build();

        JwsHeader header = JwsHeader.with(() -> "RS256").build();
        JwtEncoderParameters parameters = JwtEncoderParameters.from(header, claims);

        return jwtEncoder.encode(parameters).getTokenValue();
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String userId, String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(userId)
                .claim("username", username)
                .claim("type", "refresh")
                .build();

        JwsHeader header = JwsHeader.with(() -> "RS256").build();
        JwtEncoderParameters parameters = JwtEncoderParameters.from(header, claims);

        return jwtEncoder.encode(parameters).getTokenValue();
    }

    /**
     * 验证访问令牌
     */
    public boolean validateAccessToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String tokenType = jwt.getClaimAsString("type");

            // 检查是否为访问令牌且未过期
            return "access".equals(tokenType) &&
                   jwt.getExpiresAt() != null &&
                   jwt.getExpiresAt().isAfter(Instant.now());
        } catch (JwtException e) {
            log.debug("访问令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证刷新令牌
     */
    public boolean validateRefreshToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String tokenType = jwt.getClaimAsString("type");

            // 检查是否为刷新令牌且未过期
            return "refresh".equals(tokenType) &&
                   jwt.getExpiresAt() != null &&
                   jwt.getExpiresAt().isAfter(Instant.now());
        } catch (JwtException e) {
            log.debug("刷新令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从令牌中提取用户ID
     */
    public String extractUserIdFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getSubject();
        } catch (JwtException e) {
            log.debug("提取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中提取用户名
     */
    public String extractUsernameFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getClaimAsString("username");
        } catch (JwtException e) {
            log.debug("提取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中提取所有声明
     */
    public Map<String, Object> extractAllClaims(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getClaims();
        } catch (JwtException e) {
            log.debug("提取声明失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取访问令牌过期时间（秒）
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * 获取刷新令牌过期时间（秒）
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}