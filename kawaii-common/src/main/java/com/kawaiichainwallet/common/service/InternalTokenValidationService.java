package com.kawaiichainwallet.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 内部Token验证服务 - 通用版本
 * 统一管理微服务间的内部认证Token验证逻辑
 * 可被所有微服务引用，不依赖特定模块的类
 */
@Slf4j
@Service
public class InternalTokenValidationService {

    @Value("${app.security.internal-token:}")
    private String configuredInternalToken;

    @Value("${app.security.internal-token-validation.log-validation-failures:true}")
    private boolean logValidationFailures;

    /**
     * 验证内部Token是否有效
     *
     * @param internalToken 待验证的内部Token
     * @return true表示Token有效，false表示无效
     */
    public boolean isValidInternalToken(String internalToken) {
        if (internalToken == null || internalToken.trim().isEmpty()) {
            if (logValidationFailures) {
                log.warn("内部Token为空或null");
            }
            return false;
        }

        // 验证静态Token
        if (configuredInternalToken != null && !configuredInternalToken.isEmpty()) {
            if (configuredInternalToken.equals(internalToken)) {
                log.debug("内部Token验证通过");
                return true;
            }
        }

        // 静态Token验证失败
        if (logValidationFailures) {
            log.warn("内部Token验证失败: {}", maskToken(internalToken));
        }
        return false;
    }

    /**
     * 脱敏显示Token用于日志记录
     *
     * @param token 原始Token
     * @return 脱敏后的Token
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }
}