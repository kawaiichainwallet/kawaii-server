package com.kawaiichainwallet.user.config;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Spring Security配置
 * 简化版配置，信任Gateway的认证结果，但保留JWT生成能力
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.security.jwt.private-key}")
    private String privateKeyPem;

    @Value("${app.security.jwt.public-key}")
    private String publicKeyPem;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 简化的Security过滤器链
     * 信任Gateway的认证结果，只处理开发调试路径
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（前后端分离项目不需要）
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用Session（无状态服务）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 开发调试相关路径
                .requestMatchers(
                    "/actuator/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/error"
                ).permitAll()
                // 信任Gateway认证的所有其他请求
                .anyRequest().permitAll()
            );

        return http.build();
    }

    /**
     * JWT解码器 - 使用EC公钥验证JWT
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            ECPublicKey publicKey = loadECPublicKey(publicKeyPem);

            // 创建ECKey并封装为JWKSource
            ECKey ecKey = new ECKey.Builder(com.nimbusds.jose.jwk.Curve.P_256, publicKey).build();
            JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(ecKey));

            // 创建JWTProcessor来处理ES256签名验证
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                com.nimbusds.jose.JWSAlgorithm.ES256,
                jwkSource
            );
            jwtProcessor.setJWSKeySelector(keySelector);

            return new NimbusJwtDecoder(jwtProcessor);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load EC public key", e);
        }
    }

    /**
     * JWT编码器 - 使用EC私钥生成JWT (ES256算法)
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        try {
            ECPrivateKey privateKey = loadECPrivateKey(privateKeyPem);
            ECPublicKey publicKey = loadECPublicKey(publicKeyPem);

            ECKey ecKey = new ECKey.Builder(com.nimbusds.jose.jwk.Curve.P_256, publicKey)
                    .privateKey(privateKey)
                    .build();

            JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(ecKey));
            return new NimbusJwtEncoder(jwks);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load EC keys", e);
        }
    }

    /**
     * 加载EC私钥 (PKCS#8格式)
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
}