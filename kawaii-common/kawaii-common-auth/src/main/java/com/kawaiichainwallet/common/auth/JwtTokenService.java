package com.kawaiichainwallet.common.auth;

import com.kawaiichainwallet.common.core.exception.JwtException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * JWT Token生成服务 - 公共认证模块
 * 使用ES256算法和EC私钥生成JWT Token
 * <p>
 * 此服务仅在需要生成Token的服务中使用（如user服务）
 * 使用Nimbus JOSE库，与JwtValidationService技术栈统一
 * <p>
 * 条件加载：仅当配置了private-key时才创建此Bean
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.security.jwt.private-key")
public class JwtTokenService {

    private final JWSSigner jwtSigner;
    private final String issuer;
    /**
     * -- GETTER --
     *  获取访问令牌过期时间（秒）
     */
    @Getter
    private final long accessTokenExpiration;
    /**
     * -- GETTER --
     *  获取刷新令牌过期时间（秒）
     */
    @Getter
    private final long refreshTokenExpiration;

    public JwtTokenService(
            @Value("${app.security.jwt.private-key}") String privateKeyPem,
            @Value("${app.security.jwt.issuer:kawaii-wallet}") String issuer,
            @Value("${app.security.jwt.access-token-expiration:3600}") long accessTokenExpiration,
            @Value("${app.security.jwt.refresh-token-expiration:604800}") long refreshTokenExpiration) {
        try {
            ECPrivateKey privateKey = loadECPrivateKey(privateKeyPem);
            this.jwtSigner = new ECDSASigner(privateKey);
            this.issuer = issuer;
            this.accessTokenExpiration = accessTokenExpiration;
            this.refreshTokenExpiration = refreshTokenExpiration;
        } catch (Exception e) {
            throw new JwtException("Failed to initialize JWT token service", e);
        }
    }

    /**
     * 加载EC私钥
     */
    private ECPrivateKey loadECPrivateKey(String pemKey) throws Exception {
        String privateKeyPEM = pemKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    /**
     * 生成访问令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roles    角色列表（逗号分隔）
     * @return JWT Token字符串
     */
    public String generateAccessToken(long userId, String username, String roles) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + accessTokenExpiration * 1000);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roles", roles)
                .claim("tokenType", "access")
                .issueTime(now)
                .expirationTime(expiresAt)
                .build();

        return signToken(claims);
    }

    /**
     * 生成刷新令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Token字符串
     */
    public String generateRefreshToken(long userId, String username) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + refreshTokenExpiration * 1000);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("tokenType", "refresh")
                .issueTime(now)
                .expirationTime(expiresAt)
                .build();

        return signToken(claims);
    }

    /**
     * 签名Token
     */
    private String signToken(JWTClaimsSet claims) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.ES256);
            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(jwtSigner);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Failed to sign JWT token", e);
            throw new JwtException("Failed to sign JWT token", e);
        }
    }

}
