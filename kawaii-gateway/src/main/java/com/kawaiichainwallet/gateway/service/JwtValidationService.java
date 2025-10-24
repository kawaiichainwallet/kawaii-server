package com.kawaiichainwallet.gateway.service;

import com.kawaiichainwallet.common.core.exception.JwtException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;

/**
 * JWT验证服务 - 使用ES256算法和EC公钥验证
 */
@Slf4j
@Service
public class JwtValidationService {

    private final JWSVerifier jwtVerifier;

    public JwtValidationService(@Value("${app.security.jwt.public-key}") String publicKeyPem) {
        try {
            // 加载EC公钥并创建ES256验证器
            ECPublicKey publicKey = loadECPublicKey(publicKeyPem);
            this.jwtVerifier = new ECDSAVerifier(publicKey);
        } catch (Exception e) {
            throw new JwtException("Failed to initialize JWT verifier", e);
        }
    }

    /**
     * 加载EC公钥
     */
    private ECPublicKey loadECPublicKey(String pemKey) throws Exception {
        String publicKeyPEM = pemKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (ECPublicKey) keyFactory.generatePublic(keySpec);
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
            // JWT的subject字段存储的是userId
            return claims.getSubject();
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
     * 从JWT Token中提取角色信息
     */
    public String getRolesFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getStringClaim("roles");
        } catch (ParseException e) {
            log.debug("Failed to extract roles from token: {}", e.getMessage());
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