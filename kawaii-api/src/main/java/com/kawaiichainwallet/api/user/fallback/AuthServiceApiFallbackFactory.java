package com.kawaiichainwallet.api.user.fallback;

import com.kawaiichainwallet.api.user.client.AuthServiceApi;
import com.kawaiichainwallet.api.user.dto.TokenValidationResponse;
import com.kawaiichainwallet.common.core.response.R;
import com.kawaiichainwallet.common.core.enums.ApiCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 认证服务API降级处理工厂
 */
@Slf4j
@Component
public class AuthServiceApiFallbackFactory implements FallbackFactory<AuthServiceApi> {

    @Override
    public AuthServiceApi create(Throwable cause) {
        return new AuthServiceApi() {
            @Override
            public R<Boolean> checkAuthentication(long userId) {
                log.error("检查用户认证状态失败: userId={}", userId, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }

            @Override
            public R<Void> revokeUserTokens(String userId, String reason) {
                log.error("撤销用户Token失败: userId={}, reason={}", userId, reason, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }

            @Override
            public R<Boolean> verifyPassword(String userId, String password) {
                log.error("验证用户密码失败: userId={}", userId, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }
        };
    }
}