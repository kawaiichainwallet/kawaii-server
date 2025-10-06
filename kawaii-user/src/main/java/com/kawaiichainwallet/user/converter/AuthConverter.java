package com.kawaiichainwallet.user.converter;

import com.kawaiichainwallet.user.dto.LoginResponse;
import com.kawaiichainwallet.user.dto.RegisterResponse;
import com.kawaiichainwallet.user.entity.User;
import org.mapstruct.*;

/**
 * 认证相关的对象转换器
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AuthConverter {

    /**
     * User实体转换为LoginResponse
     */
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "expiresIn", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "email", ignore = true)
    LoginResponse userToLoginResponse(User user);

    /**
     * User实体转换为RegisterResponse
     */
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "email", ignore = true)
    RegisterResponse userToRegisterResponse(User user);

    /**
     * 更新LoginResponse后处理脱敏
     */
    @AfterMapping
    default void maskSensitiveInfo(@MappingTarget LoginResponse target, User user) {
        if (user.getPhone() != null) {
            target.setPhone(maskPhone(user.getPhone()));
        }
        if (user.getEmail() != null) {
            target.setEmail(maskEmail(user.getEmail()));
        }
    }

    /**
     * 更新RegisterResponse后处理脱敏
     */
    @AfterMapping
    default void maskSensitiveInfo(@MappingTarget RegisterResponse target, User user) {
        if (user.getPhone() != null) {
            target.setPhone(maskPhone(user.getPhone()));
        }
        if (user.getEmail() != null) {
            target.setEmail(maskEmail(user.getEmail()));
        }
    }

    /**
     * 创建基础LoginResponse（用于refreshToken场景）
     */
    default LoginResponse createLoginResponse(long userId, String username) {
        LoginResponse response = new LoginResponse();
        response.setUserId(userId);
        response.setUsername(username);
        response.setTokenType("Bearer");
        return response;
    }

    /**
     * 手机号脱敏
     */
    @Named("maskPhone")
    default String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏
     */
    @Named("maskEmail")
    default String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return username.charAt(0) + "***@" + domain;
        } else {
            return username.charAt(0) + "****" + username.charAt(username.length() - 1) + "@" + domain;
        }
    }
}