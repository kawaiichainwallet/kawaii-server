package com.kawaiichainwallet.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret:kawaii-chain-wallet-secret-key-2025}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration:900000}")  // 15分钟
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration:604800000}")  // 7天
    private long refreshTokenExpiration;

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(String userId, String username) {
        return generateToken(userId, username, TOKEN_TYPE_ACCESS, accessTokenExpiration);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String userId, String username) {
        return generateToken(userId, username, TOKEN_TYPE_REFRESH, refreshTokenExpiration);
    }

    /**
     * 生成JWT令牌
     */
    private String generateToken(String userId, String username, String tokenType, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_TOKEN_TYPE, tokenType);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从JWT令牌中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get(CLAIM_USER_ID, String.class) : null;
    }

    /**
     * 从JWT令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get(CLAIM_USERNAME, String.class) : null;
    }

    /**
     * 从JWT令牌中获取令牌类型
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get(CLAIM_TOKEN_TYPE, String.class) : null;
    }

    /**
     * 从JWT令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 从JWT令牌中获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.debug("无法解析JWT令牌: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证JWT令牌
     */
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            log.debug("JWT签名无效: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("JWT格式错误: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.debug("JWT令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("不支持的JWT令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT令牌为空: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 验证访问令牌
     */
    public boolean validateAccessToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        String tokenType = getTokenTypeFromToken(token);
        return TOKEN_TYPE_ACCESS.equals(tokenType);
    }

    /**
     * 验证刷新令牌
     */
    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        String tokenType = getTokenTypeFromToken(token);
        return TOKEN_TYPE_REFRESH.equals(tokenType);
    }

    /**
     * 检查令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    /**
     * 从请求头中提取JWT令牌
     */
    public String extractTokenFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}