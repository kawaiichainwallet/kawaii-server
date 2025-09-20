package com.kawaiichainwallet.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * 一次性密码服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String ATTEMPT_KEY_PREFIX = "otp_attempts:";
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    private static final int BLOCK_DURATION_MINUTES = 15;

    /**
     * 生成并发送OTP验证码
     */
    public String generateAndStoreOtp(String identifier) {
        // 检查是否被阻止
        if (isBlocked(identifier)) {
            throw new RuntimeException("验证码发送过于频繁，请稍后重试");
        }

        String otp = generateOtp();
        String key = OTP_KEY_PREFIX + identifier;

        // 存储OTP，设置5分钟过期
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(OTP_EXPIRY_MINUTES));

        log.info("生成OTP验证码: identifier={}, otp={}", identifier, otp);
        return otp;
    }

    /**
     * 验证OTP验证码
     */
    public boolean validateOtp(String identifier, String providedOtp) {
        String key = OTP_KEY_PREFIX + identifier;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            log.warn("OTP验证失败 - 验证码不存在或已过期: identifier={}", identifier);
            return false;
        }

        if (storedOtp.equals(providedOtp)) {
            // 验证成功，删除验证码和失败记录
            redisTemplate.delete(key);
            clearAttempts(identifier);
            log.info("OTP验证成功: identifier={}", identifier);
            return true;
        } else {
            // 验证失败，记录尝试次数
            incrementAttempts(identifier);
            log.warn("OTP验证失败 - 验证码不匹配: identifier={}", identifier);
            return false;
        }
    }

    /**
     * 生成6位数字验证码
     */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }

    /**
     * 检查是否被阻止
     */
    private boolean isBlocked(String identifier) {
        String attemptKey = ATTEMPT_KEY_PREFIX + identifier;
        String attempts = redisTemplate.opsForValue().get(attemptKey);

        if (attempts != null) {
            int attemptCount = Integer.parseInt(attempts);
            return attemptCount >= MAX_ATTEMPTS;
        }

        return false;
    }

    /**
     * 增加失败尝试次数
     */
    private void incrementAttempts(String identifier) {
        String attemptKey = ATTEMPT_KEY_PREFIX + identifier;
        String attempts = redisTemplate.opsForValue().get(attemptKey);

        int currentAttempts = attempts != null ? Integer.parseInt(attempts) : 0;
        currentAttempts++;

        if (currentAttempts >= MAX_ATTEMPTS) {
            // 达到最大尝试次数，阻止15分钟
            redisTemplate.opsForValue().set(attemptKey, String.valueOf(currentAttempts),
                    Duration.ofMinutes(BLOCK_DURATION_MINUTES));
        } else {
            // 未达到最大次数，设置5分钟过期
            redisTemplate.opsForValue().set(attemptKey, String.valueOf(currentAttempts),
                    Duration.ofMinutes(OTP_EXPIRY_MINUTES));
        }
    }

    /**
     * 验证OTP验证码（带类型和IP参数）
     */
    public boolean verifyOtp(String identifier, String otp, String type, String clientIp) {
        log.info("验证OTP: identifier={}, type={}, clientIp={}", identifier, type, clientIp);
        return validateOtp(identifier, otp);
    }

    /**
     * 发送OTP验证码
     */
    public String sendOtp(String identifier, String type, String clientIp) {
        log.info("发送OTP: identifier={}, type={}, clientIp={}", identifier, type, clientIp);
        return generateAndStoreOtp(identifier);
    }

    /**
     * 清除失败尝试记录
     */
    private void clearAttempts(String identifier) {
        String attemptKey = ATTEMPT_KEY_PREFIX + identifier;
        redisTemplate.delete(attemptKey);
    }
}