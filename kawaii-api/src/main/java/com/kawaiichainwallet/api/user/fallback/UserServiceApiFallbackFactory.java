package com.kawaiichainwallet.api.user.fallback;

import com.kawaiichainwallet.api.user.client.UserServiceApi;
import com.kawaiichainwallet.api.user.dto.UserInfoResponse;
import com.kawaiichainwallet.api.user.dto.UserPaymentPermissionResponse;
import com.kawaiichainwallet.common.core.enums.ApiCode;
import com.kawaiichainwallet.common.core.response.R;
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
            public R<UserInfoResponse> getUserInfo(long userId) {
                log.error("获取用户信息失败: userId={}", userId, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }

            @Override
            public R<UserInfoResponse> getUserByUsername(String username) {
                log.error("根据用户名获取用户信息失败: username={}", username, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }

            @Override
            public R<UserInfoResponse> getUserByEmail(String email) {
                log.error("根据邮箱获取用户信息失败: email={}", email, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }

            @Override
            public R<List<UserInfoResponse>> getBatchUsers(List<String> userIds) {
                log.error("批量获取用户信息失败: userIds={}", userIds, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }

            @Override
            public R<Boolean> userExists(long userId) {
                log.error("检查用户是否存在失败: userId={}", userId, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }

            @Override
            public R<UserPaymentPermissionResponse> getUserPaymentPermission(String userId) {
                log.error("获取用户支付权限失败: userId={}", userId, cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE);
            }
        };
    }
}