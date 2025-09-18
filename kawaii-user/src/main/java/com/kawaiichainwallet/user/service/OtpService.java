package com.kawaiichainwallet.user.service;

import com.kawaiichainwallet.common.enums.ApiCode;
import com.kawaiichainwallet.common.exception.BusinessException;
import com.kawaiichainwallet.common.utils.CryptoUtil;
import com.kawaiichainwallet.common.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * OTP验证码服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.expiration:300}")
    private long otpExpiration; // 秒

    @Value("${app.otp.rate-limit:60}")
    private long rateLimitSeconds; // 秒

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    private static final String OTP_PREFIX = "otp:";
    private static final String RATE_LIMIT_PREFIX = "otp_rate:";
    private static final String ATTEMPT_PREFIX = "otp_attempt:";

    /**
     * 发送OTP验证码
     */
    public void sendOtp(String target, String type, String purpose) {
        // 验证输入参数
        validateSendOtpParams(target, type, purpose);

        // 检查发送频率限制
        checkRateLimit(target, type, purpose);

        // 生成验证码
        String otpCode = CryptoUtil.generateNumericCode(otpLength);

        // 存储验证码到Redis
        String otpKey = buildOtpKey(target, type, purpose);
        redisTemplate.opsForValue().set(otpKey, otpCode, otpExpiration, TimeUnit.SECONDS);

        // 设置发送频率限制
        String rateLimitKey = buildRateLimitKey(target, type, purpose);
        redisTemplate.opsForValue().set(rateLimitKey, "1", rateLimitSeconds, TimeUnit.SECONDS);

        // 重置验证尝试次数
        String attemptKey = buildAttemptKey(target, type, purpose);
        redisTemplate.delete(attemptKey);

        // 发送验证码
        if ("phone".equals(type)) {
            sendSmsOtp(target, otpCode, purpose);
        } else if ("email".equals(type)) {
            sendEmailOtp(target, otpCode, purpose);
        }

        log.info("OTP发送成功: target={}, type={}, purpose={}",
                 ValidationUtil.maskPhone(target), type, purpose);
    }

    /**
     * 验证OTP验证码
     */
    public boolean verifyOtp(String target, String type, String purpose, String inputCode) {
        // 验证输入参数
        validateVerifyOtpParams(target, type, purpose, inputCode);

        // 检查验证尝试次数
        checkAttemptLimit(target, type, purpose);

        // 获取存储的验证码
        String otpKey = buildOtpKey(target, type, purpose);
        String storedCode = redisTemplate.opsForValue().get(otpKey);

        if (!StringUtils.hasText(storedCode)) {
            throw new BusinessException(ApiCode.OTP_NOT_FOUND, "验证码不存在或已过期");
        }

        // 增加验证尝试次数
        incrementAttemptCount(target, type, purpose);

        // 验证码比较
        boolean isValid = storedCode.equals(inputCode);

        if (isValid) {
            // 验证成功，删除相关缓存
            redisTemplate.delete(otpKey);
            redisTemplate.delete(buildAttemptKey(target, type, purpose));
            redisTemplate.delete(buildRateLimitKey(target, type, purpose));

            log.info("OTP验证成功: target={}, type={}, purpose={}",
                     ValidationUtil.maskPhone(target), type, purpose);
        } else {
            log.warn("OTP验证失败: target={}, type={}, purpose={}, inputCode={}",
                     ValidationUtil.maskPhone(target), type, purpose, inputCode);
        }

        return isValid;
    }

    /**
     * 检查发送频率限制
     */
    private void checkRateLimit(String target, String type, String purpose) {
        String rateLimitKey = buildRateLimitKey(target, type, purpose);
        String rateLimitValue = redisTemplate.opsForValue().get(rateLimitKey);

        if (StringUtils.hasText(rateLimitValue)) {
            Long ttl = redisTemplate.getExpire(rateLimitKey, TimeUnit.SECONDS);
            throw new BusinessException(ApiCode.OTP_SEND_TOO_FREQUENT,
                    String.format("验证码发送过于频繁，请等待 %d 秒后重试", ttl != null ? ttl : rateLimitSeconds));
        }
    }

    /**
     * 检查验证尝试次数限制
     */
    private void checkAttemptLimit(String target, String type, String purpose) {
        String attemptKey = buildAttemptKey(target, type, purpose);
        String attemptValue = redisTemplate.opsForValue().get(attemptKey);

        if (StringUtils.hasText(attemptValue)) {
            int currentAttempts = Integer.parseInt(attemptValue);
            if (currentAttempts >= maxAttempts) {
                throw new BusinessException(ApiCode.OTP_VERIFY_TOO_MANY_ATTEMPTS,
                        "验证码验证次数过多，请重新获取");
            }
        }
    }

    /**
     * 增加验证尝试次数
     */
    private void incrementAttemptCount(String target, String type, String purpose) {
        String attemptKey = buildAttemptKey(target, type, purpose);
        redisTemplate.opsForValue().increment(attemptKey);
        redisTemplate.expire(attemptKey, otpExpiration, TimeUnit.SECONDS);
    }

    /**
     * 验证发送OTP参数
     */
    private void validateSendOtpParams(String target, String type, String purpose) {
        if (!StringUtils.hasText(target)) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "接收目标不能为空");
        }

        if ("phone".equals(type) && !ValidationUtil.isValidPhone(target)) {
            throw new BusinessException(ApiCode.INVALID_PHONE_FORMAT);
        }

        if ("email".equals(type) && !ValidationUtil.isValidEmail(target)) {
            throw new BusinessException(ApiCode.INVALID_EMAIL_FORMAT);
        }

        if (!StringUtils.hasText(type) || (!type.equals("phone") && !type.equals("email"))) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "验证码类型只能是phone或email");
        }

        if (!StringUtils.hasText(purpose)) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "业务类型不能为空");
        }
    }

    /**
     * 验证验证OTP参数
     */
    private void validateVerifyOtpParams(String target, String type, String purpose, String inputCode) {
        validateSendOtpParams(target, type, purpose);

        if (!StringUtils.hasText(inputCode)) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "验证码不能为空");
        }

        if (!ValidationUtil.isValidNumericCode(inputCode)) {
            throw new BusinessException(ApiCode.OTP_INVALID, "验证码格式不正确");
        }
    }

    /**
     * 发送短信验证码
     */
    private void sendSmsOtp(String phone, String otpCode, String purpose) {
        // TODO: 集成阿里云SMS服务
        log.info("发送短信验证码: phone={}, code={}, purpose={}",
                 ValidationUtil.maskPhone(phone), otpCode, purpose);

        // 临时实现：在开发环境下直接在日志中输出验证码
        if (log.isDebugEnabled()) {
            log.debug("【开发环境】短信验证码: {}", otpCode);
        }
    }

    /**
     * 发送邮件验证码
     */
    private void sendEmailOtp(String email, String otpCode, String purpose) {
        // TODO: 集成邮件服务
        log.info("发送邮件验证码: email={}, code={}, purpose={}",
                 ValidationUtil.maskEmail(email), otpCode, purpose);

        // 临时实现：在开发环境下直接在日志中输出验证码
        if (log.isDebugEnabled()) {
            log.debug("【开发环境】邮件验证码: {}", otpCode);
        }
    }

    /**
     * 构建OTP缓存Key
     */
    private String buildOtpKey(String target, String type, String purpose) {
        return OTP_PREFIX + type + ":" + purpose + ":" + target;
    }

    /**
     * 构建频率限制缓存Key
     */
    private String buildRateLimitKey(String target, String type, String purpose) {
        return RATE_LIMIT_PREFIX + type + ":" + purpose + ":" + target;
    }

    /**
     * 构建尝试次数缓存Key
     */
    private String buildAttemptKey(String target, String type, String purpose) {
        return ATTEMPT_PREFIX + type + ":" + purpose + ":" + target;
    }
}