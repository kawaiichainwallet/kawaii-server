package com.kawaiichainwallet.user.converter;

import com.kawaiichainwallet.api.user.dto.UserInfoResponse;
import com.kawaiichainwallet.user.dto.UserDetailsDto;
import com.kawaiichainwallet.user.entity.User;
import com.kawaiichainwallet.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 服务API对象转换器
 * 用于处理服务间调用的DTO转换
 */
@Mapper(componentModel = "spring")
public interface ServiceApiConverter {

    ServiceApiConverter INSTANCE = Mappers.getMapper(ServiceApiConverter.class);

    /**
     * User实体转换为UserInfoResponse（含脱敏处理）
     */
    @Mapping(target = "phone", source = "phone", qualifiedByName = "maskPhone")
    @Mapping(target = "email", source = "email", qualifiedByName = "maskEmail")
    @Mapping(target = "roles", ignore = true) // 需要从其他地方获取角色信息
    @Mapping(target = "kycLevel", ignore = true) // 需要从UserProfile获取
    @Mapping(target = "displayName", ignore = true) // 需要从UserProfile获取
    @Mapping(target = "avatarUrl", ignore = true) // 需要从UserProfile获取
    @Mapping(target = "language", ignore = true) // 需要从UserProfile获取
    @Mapping(target = "timezone", ignore = true) // 需要从UserProfile获取
    @Mapping(target = "currency", ignore = true) // 需要从UserProfile获取
    @Mapping(target = "paymentEnabled", ignore = true) // 需要从UserProfile获取
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lastLoginAt", source = "lastLoginAt")
    UserInfoResponse userToApiDto(User user);

    /**
     * User和UserProfile合并转换为UserInfoResponse
     */
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "phone", source = "user.phone", qualifiedByName = "maskPhone")
    @Mapping(target = "email", source = "user.email", qualifiedByName = "maskEmail")
    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "emailVerified", source = "user.emailVerified")
    @Mapping(target = "phoneVerified", source = "user.phoneVerified")
    @Mapping(target = "twoFactorEnabled", source = "user.twoFactorEnabled")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "lastLoginAt", source = "user.lastLoginAt")
    @Mapping(target = "displayName", source = "userProfile.displayName")
    @Mapping(target = "avatarUrl", source = "userProfile.avatarUrl")
    @Mapping(target = "language", source = "userProfile.language")
    @Mapping(target = "timezone", source = "userProfile.timezone")
    @Mapping(target = "currency", source = "userProfile.currency")
    @Mapping(target = "kycLevel", ignore = true) // UserProfile中没有kycLevel字段，需要从其他地方获取
    @Mapping(target = "roles", ignore = true) // 需要单独设置
    @Mapping(target = "paymentEnabled", ignore = true)
    // 需要单独设置
    UserInfoResponse userAndProfileToApiDto(User user, UserProfile userProfile);

    /**
     * UserDetailsDto转换为UserInfoResponse (API层)
     */
    @Mapping(target = "roles", ignore = true) // 需要单独设置
    @Mapping(target = "kycLevel", ignore = true) // 需要单独设置
    @Mapping(target = "paymentEnabled", ignore = true) // 需要单独设置
    UserInfoResponse userDetailsToApiDto(UserDetailsDto userDetails);

    /**
     * TokenValidationResponse转换为TokenValidationResponse (API层)
     */
    @Mapping(target = "valid", source = "valid")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "roles", ignore = true) // TokenValidationResponse中没有roles字段
    @Mapping(target = "tokenType", source = "tokenType")
    @Mapping(target = "expiresAt", source = "expiresAt", qualifiedByName = "timestampToLocalDateTime")
    @Mapping(target = "errorMessage", source = "errorMessage")
    @Mapping(target = "errorCode", ignore = true)
    // TokenValidationResponse中没有errorCode字段
    com.kawaiichainwallet.api.user.dto.TokenValidationResponse validationResponseToApiDto(com.kawaiichainwallet.user.dto.TokenValidationResponse response);

    /**
     * 创建失败的TokenValidationResponse (API层)
     */
    default com.kawaiichainwallet.api.user.dto.TokenValidationResponse createFailedValidationResponse(String errorMessage, String errorCode) {
        com.kawaiichainwallet.api.user.dto.TokenValidationResponse dto = new com.kawaiichainwallet.api.user.dto.TokenValidationResponse();
        dto.setValid(false);
        dto.setErrorMessage(errorMessage);
        dto.setErrorCode(errorCode);
        return dto;
    }

    /**
     * 创建成功的TokenValidationResponse (API层)
     */
    default com.kawaiichainwallet.api.user.dto.TokenValidationResponse createSuccessValidationResponse(String userId, String username, List<String> roles) {
        com.kawaiichainwallet.api.user.dto.TokenValidationResponse dto = new com.kawaiichainwallet.api.user.dto.TokenValidationResponse();
        dto.setValid(true);
        dto.setUserId(userId);
        dto.setUsername(username);
        dto.setRoles(roles);
        dto.setTokenType("access");
        return dto;
    }

    /**
     * 手机号脱敏处理
     */
    @Named("maskPhone")
    default String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏处理
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
            return username.charAt(0) + "*@" + domain;
        }

        return username.substring(0, 2) + "***@" + domain;
    }

    /**
     * 时间戳转LocalDateTime
     */
    @Named("timestampToLocalDateTime")
    default LocalDateTime timestampToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}