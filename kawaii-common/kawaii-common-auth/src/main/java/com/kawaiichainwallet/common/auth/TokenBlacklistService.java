package com.kawaiichainwallet.common.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务（公共模块）
 * 用于管理已登出或需要撤销的JWT Token
 * <p>
 * 可被User服务、Gateway等多个微服务复用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";

    private final StringRedisTemplate redisTemplate;
    private final JwtValidationService jwtValidationService;

    /**
     * 将Token加入黑名单
     *
     * @param token JWT token
     */
    public void addToBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("尝试将空token加入黑名单");
            return;
        }

        try {
            // 计算token的剩余有效时间
            long expirationTime = jwtValidationService.getExpirationTime(token);
            long currentTime = System.currentTimeMillis();
            long ttl = expirationTime - currentTime;

            // 如果token已经过期，不需要加入黑名单
            if (ttl <= 0) {
                log.debug("Token已过期，无需加入黑名单");
                return;
            }

            // 使用token的后缀作为key（避免key过长）
            String tokenSuffix = getTokenSuffix(token);
            String key = BLACKLIST_KEY_PREFIX + tokenSuffix;

            // 将token加入黑名单，设置过期时间为token的剩余有效时间
            redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);

            log.info("Token已加入黑名单: key={}, ttl={}ms", key, ttl);
        } catch (Exception e) {
            log.error("将token加入黑名单失败", e);
            throw e;
        }
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param token JWT token
     * @return true表示在黑名单中（已失效），false表示不在黑名单中
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            String tokenSuffix = getTokenSuffix(token);
            String key = BLACKLIST_KEY_PREFIX + tokenSuffix;
            Boolean exists = redisTemplate.hasKey(key);

            boolean blacklisted = Boolean.TRUE.equals(exists);
            if (blacklisted) {
                log.debug("Token在黑名单中: key={}", key);
            }

            return blacklisted;
        } catch (Exception e) {
            log.error("检查token黑名单状态失败", e);
            // 发生异常时，为了安全起见，认为token无效
            return true;
        }
    }

    /**
     * 从token中提取后缀作为Redis key
     * 使用token的最后32个字符（或全部，如果不足32个字符）
     */
    private String getTokenSuffix(String token) {
        if (token.length() <= 32) {
            return token;
        }
        return token.substring(token.length() - 32);
    }

    /**
     * 清除指定用户的所有token（强制登出）
     *
     * @param userId 用户ID
     */
    public void clearUserTokens(Long userId) {
        try {
            String pattern = BLACKLIST_KEY_PREFIX + "user:" + userId + ":*";
            redisTemplate.keys(pattern).forEach(key -> {
                redisTemplate.delete(key);
                log.info("删除用户token: userId={}, key={}", userId, key);
            });
        } catch (Exception e) {
            log.error("清除用户token失败: userId={}", userId, e);
        }
    }
}
