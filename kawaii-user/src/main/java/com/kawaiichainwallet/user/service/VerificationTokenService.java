package com.kawaiichainwallet.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证Token服务 - 管理OTP验证后的临时Token
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private final StringRedisTemplate redisTemplate;

    private static final String VERIFICATION_TOKEN_PREFIX = "verification:token:";
    private static final long TOKEN_EXPIRATION_MINUTES = 10;

    /**
     * 生成验证Token
     *
     * @param target  目标(手机号或邮箱)
     * @param type    类型(phone或email)
     * @param purpose 用途(register、reset_password等)
     * @return 生成的验证Token
     */
    public String generateToken(String target, String type, String purpose) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = VERIFICATION_TOKEN_PREFIX + token;

        // 存储Token关联的信息: target|type|purpose
        String value = String.format("%s|%s|%s", target, type, purpose);
        redisTemplate.opsForValue().set(key, value, TOKEN_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        log.info("生成验证Token: token={}, target={}, type={}, purpose={}", token, target, type, purpose);
        return token;
    }

    /**
     * 验证Token并获取关联信息
     *
     * @param token   验证Token
     * @param target  目标(手机号或邮箱)
     * @param purpose 用途
     * @return 是否验证成功
     */
    public boolean verifyToken(String token, String target, String purpose) {
        if (token == null || token.isEmpty()) {
            log.warn("验证Token失败: Token为空");
            return false;
        }

        String key = VERIFICATION_TOKEN_PREFIX + token;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            log.warn("验证Token失败: Token不存在或已过期, token={}", token);
            return false;
        }

        // 解析Token关联的信息
        String[] parts = value.split("\\|");
        if (parts.length != 3) {
            log.error("验证Token失败: Token数据格式错误, token={}, value={}", token, value);
            return false;
        }

        String storedTarget = parts[0];
        String storedPurpose = parts[2];

        // 验证target和purpose是否匹配
        if (!storedTarget.equals(target) || !storedPurpose.equals(purpose)) {
            log.warn("验证Token失败: 信息不匹配, token={}, target={}, purpose={}", token, target, purpose);
            return false;
        }

        log.info("验证Token成功: token={}, target={}, purpose={}", token, target, purpose);
        return true;
    }

    /**
     * 使用Token(验证成功后删除,防止重复使用)
     *
     * @param token 验证Token
     */
    public void consumeToken(String token) {
        String key = VERIFICATION_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
        log.info("Token已使用并删除: token={}", token);
    }

    /**
     * 获取Token关联的target
     *
     * @param token 验证Token
     * @return target(手机号或邮箱)
     */
    public String getTargetFromToken(String token) {
        String key = VERIFICATION_TOKEN_PREFIX + token;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        String[] parts = value.split("\\|");
        return parts.length >= 1 ? parts[0] : null;
    }
}
