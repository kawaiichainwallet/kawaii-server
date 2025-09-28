package com.kawaiichainwallet.common.business.auth;

import com.kawaiichainwallet.common.utils.TimeUtil;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * 统一JWT Token管理器
 * 提供Token生成、验证、解析等功能
 */
@Slf4j
@Service
public class JwtTokenManager {

    private final JWSSigner jwtSigner;
    private final JWSVerifier jwtVerifier;

    @Value("${app.security.jwt.issuer:kawaii-wallet-auth}")
    private String issuer;

    @Value("${app.security.jwt.access-token-expiration:900}") // 15分钟
    private long accessTokenExpiration;

    @Value("${app.security.jwt.refresh-token-expiration:2592000}") // 30天
    private long refreshTokenExpiration;

    public JwtTokenManager(@Value("${app.security.jwt.secret}") String jwtSecret) {
        try {
            byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            this.jwtSigner = new MACSigner(secretBytes);
            this.jwtVerifier = new MACVerifier(secretBytes);
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to initialize JWT manager", e);
        }
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(String userId, String username, String email) {
        Instant now = TimeUtil.nowInstant();
        Instant expiresAt = TimeUtil.plusSeconds(now, accessTokenExpiration);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(userId)
                .claim("username", username)
                .claim("email", email)
                .claim("type", "access")
                .claim("roles", "USER")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiresAt))
                .build();

        return signToken(claims);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String userId, String username) {
        Instant now = TimeUtil.nowInstant();
        Instant expiresAt = TimeUtil.plusSeconds(now, refreshTokenExpiration);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(userId)
                .claim("username", username)
                .claim("type", "refresh")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiresAt))
                .build();

        return signToken(claims);
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(jwtVerifier)) {
                return false;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expirationTime = claims.getExpirationTime();
            return expirationTime != null && expirationTime.after(Date.from(TimeUtil.nowInstant()));
        } catch (ParseException | JOSEException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证访问令牌
     */
    public boolean validateAccessToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        String tokenType = getTokenType(token);
        return "access".equals(tokenType);
    }

    /**
     * 验证刷新令牌
     */
    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        String tokenType = getTokenType(token);
        return "refresh".equals(tokenType);
    }

    /**
     * 从Token中提取用户ID
     */
    public String getUserIdFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getSubject();
        } catch (ParseException e) {
            log.debug("Failed to extract userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中提取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getStringClaim("username");
        } catch (ParseException e) {
            log.debug("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中提取邮箱
     */
    public String getEmailFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getStringClaim("email");
        } catch (ParseException e) {
            log.debug("Failed to extract email from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中提取类型
     */
    public String getTokenType(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getStringClaim("type");
        } catch (ParseException e) {
            log.debug("Failed to extract token type: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中提取所有声明
     */
    public Map<String, Object> getAllClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getClaims();
        } catch (ParseException e) {
            log.debug("Failed to extract claims from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Authorization header中提取Token
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 签名Token
     */
    private String signToken(JWTClaimsSet claims) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(jwtSigner);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Failed to sign JWT token", e);
            throw new RuntimeException("Failed to sign JWT token", e);
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