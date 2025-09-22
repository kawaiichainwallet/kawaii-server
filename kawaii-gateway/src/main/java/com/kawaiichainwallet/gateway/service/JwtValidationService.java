package com.kawaiichainwallet.gateway.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * JWT验证服务 - 使用Nimbus JOSE + JWT库
 */
@Slf4j
@Service
public class JwtValidationService {

    private final JWSVerifier jwtVerifier;

    public JwtValidationService(@Value("${app.security.jwt.secret}") String jwtSecret) {
        try {
            // 使用与user服务相同的密钥创建JWT验证器
            byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            this.jwtVerifier = new MACVerifier(secretBytes);
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to initialize JWT verifier", e);
        }
    }

    /**
     * 验证JWT Token
     */
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.verify(jwtVerifier);
        } catch (ParseException | JOSEException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从JWT Token中提取用户ID
     */
    public String getUserIdFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getStringClaim("userId");
        } catch (ParseException e) {
            log.debug("Failed to extract userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT Token中提取用户名
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
     * 从JWT Token中提取Token类型
     */
    public String getTokenTypeFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getStringClaim("tokenType");
        } catch (ParseException e) {
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