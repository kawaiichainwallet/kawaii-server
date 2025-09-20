package com.kawaiichainwallet.api.fallback;

import com.kawaiichainwallet.api.client.UserServiceApi;
import com.kawaiichainwallet.api.dto.TokenValidationResponse;
import com.kawaiichainwallet.api.dto.UserInfoResponse;
import com.kawaiichainwallet.api.dto.UserPaymentPermissionResponse;
import com.kawaiichainwallet.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户服务API降级处理工厂
 */
@Slf4j
@Component
public class UserServiceApiFallbackFactory implements FallbackFactory<UserServiceApi> {

    @Override
    public UserServiceApi create(Throwable cause) {
        return new UserServiceApi() {
            @Override
            public R<TokenValidationResponse> validateToken(String authHeader, String internalToken) {
                log.error("用户服务Token验证失败", cause);
                TokenValidationResponse dto = new TokenValidationResponse();
                dto.setValid(false);
                dto.setErrorMessage("服务暂时不可用");
                dto.setErrorCode("SERVICE_UNAVAILABLE");
                return R.error(500, "用户服务暂时不可用");
            }

            @Override
            public R<UserInfoResponse> getUserInfo(String userId, String internalToken) {
                log.error("获取用户信息失败: userId={}", userId, cause);
                return R.error(500, "用户服务暂时不可用");
            }

            @Override
            public R<UserInfoResponse> getUserByUsername(String username, String internalToken) {
                log.error("根据用户名获取用户信息失败: username={}", username, cause);
                return R.error(500, "用户服务暂时不可用");
            }

            @Override
            public R<UserInfoResponse> getUserByEmail(String email, String internalToken) {
                log.error("根据邮箱获取用户信息失败: email={}", email, cause);
                return R.error(500, "用户服务暂时不可用");
            }

            @Override
            public R<List<UserInfoResponse>> getBatchUsers(List<String> userIds, String internalToken) {
                log.error("批量获取用户信息失败: userIds={}", userIds, cause);
                return R.error(500, "用户服务暂时不可用");
            }

            @Override
            public R<Boolean> userExists(String userId, String internalToken) {
                log.error("检查用户是否存在失败: userId={}", userId, cause);
                return R.error(500, "用户服务暂时不可用");
            }

            @Override
            public R<UserPaymentPermissionResponse> getUserPaymentPermission(String userId, String internalToken) {
                log.error("获取用户支付权限失败: userId={}", userId, cause);
                return R.error(500, "用户服务暂时不可用");
            }
        };
    }
}