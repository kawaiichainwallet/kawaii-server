package com.kawaiichainwallet.user.converter;

import com.kawaiichainwallet.user.dto.LoginResponse;
import com.kawaiichainwallet.user.dto.RegisterResponse;
import com.kawaiichainwallet.user.dto.UserInfoResponse;
import com.kawaiichainwallet.user.entity.User;
import com.kawaiichainwallet.user.entity.UserProfile;
import org.mapstruct.*;

/**
 * 用户相关的对象转换器
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserConverter {

    /**
     * User实体转换为UserInfoResponse
     */
    @Mapping(target = "phone", ignore = true)  // 手机号需要脱敏处理
    @Mapping(target = "email", ignore = true)  // 邮箱需要脱敏处理
    UserInfoResponse userToUserInfoResponse(User user);

    /**
     * 合并User和UserProfile到UserInfoResponse
     */
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.status", target = "status")
    @Mapping(source = "user.emailVerified", target = "emailVerified")
    @Mapping(source = "user.phoneVerified", target = "phoneVerified")
    @Mapping(source = "user.twoFactorEnabled", target = "twoFactorEnabled")
    @Mapping(source = "user.createdAt", target = "createdAt")
    @Mapping(source = "userProfile.displayName", target = "displayName")
    @Mapping(source = "userProfile.avatar", target = "avatarUrl")
    @Mapping(source = "userProfile.language", target = "language")
    @Mapping(source = "userProfile.timezone", target = "timezone")
    @Mapping(source = "userProfile.currency", target = "currency")
    @Mapping(target = "phone", ignore = true)  // 手机号需要脱敏处理
    @Mapping(target = "email", ignore = true)  // 邮箱需要脱敏处理
    UserInfoResponse userAndProfileToUserInfoResponse(User user, UserProfile userProfile);

    /**
     * User实体转换为LoginResponse
     */
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "expiresIn", ignore = true)
    @Mapping(target = "phone", ignore = true)  // 手机号需要脱敏处理
    @Mapping(target = "email", ignore = true)  // 邮箱需要脱敏处理
    LoginResponse userToLoginResponse(User user);

    /**
     * User实体转换为RegisterResponse
     */
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    @Mapping(target = "phone", ignore = true)  // 手机号需要脱敏处理
    @Mapping(target = "email", ignore = true)  // 邮箱需要脱敏处理
    RegisterResponse userToRegisterResponse(User user);

    /**
     * 更新UserInfoResponse后处理脱敏
     */
    @AfterMapping
    default void maskSensitiveInfo(@MappingTarget UserInfoResponse target, User user) {
        if (user.getPhone() != null) {
            target.setPhone(maskPhone(user.getPhone()));
        }
        if (user.getEmail() != null) {
            target.setEmail(maskEmail(user.getEmail()));
        }
    }

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