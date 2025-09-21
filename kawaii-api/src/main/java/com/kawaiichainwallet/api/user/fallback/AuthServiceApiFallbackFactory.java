package com.kawaiichainwallet.api.user.fallback;

import com.kawaiichainwallet.api.user.client.AuthServiceApi;
import com.kawaiichainwallet.api.user.dto.TokenValidationResponse;
import com.kawaiichainwallet.common.response.R;
import com.kawaiichainwallet.common.enums.ApiCode;
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
            public R<TokenValidationResponse> validateToken(String authHeader, String internalToken) {
                log.error("认证服务Token验证失败", cause);
                TokenValidationResponse dto = new TokenValidationResponse();
                dto.setValid(false);
                dto.setErrorMessage("认证服务暂时不可用");
                dto.setErrorCode("SERVICE_UNAVAILABLE");
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }

            @Override
            public R<Boolean> checkAuthentication(String userId, String internalToken) {
                log.error("检查用户认证状态失败: userId={}", userId, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }

            @Override
            public R<Void> revokeUserTokens(String userId, String reason, String internalToken) {
                log.error("撤销用户Token失败: userId={}, reason={}", userId, reason, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }

            @Override
            public R<Boolean> verifyPassword(String userId, String password, String internalToken) {
                log.error("验证用户密码失败: userId={}", userId, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }
        };
    }
}