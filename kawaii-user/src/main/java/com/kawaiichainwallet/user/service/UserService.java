package com.kawaiichainwallet.user.service;

import com.kawaiichainwallet.common.enums.ApiCode;
import com.kawaiichainwallet.common.exception.BusinessException;
import com.kawaiichainwallet.common.utils.ValidationUtil;
import com.kawaiichainwallet.user.dto.UserInfoResponse;
import com.kawaiichainwallet.user.dto.UpdateUserInfoRequest;
import com.kawaiichainwallet.user.entity.User;
import com.kawaiichainwallet.user.entity.UserProfile;
import com.kawaiichainwallet.user.mapper.UserMapper;
import com.kawaiichainwallet.user.mapper.UserProfileMapper;
import com.kawaiichainwallet.user.converter.UserConverter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务 - 专注用户信息管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserConverter userConverter;

    /**
     * 根据用户ID获取用户基本信息
     */
    public User getUserById(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ApiCode.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 根据用户名查询用户
     */
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    /**
     * 根据邮箱查询用户
     */
    public User getUserByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    /**
     * 根据手机号查询用户
     */
    public User getUserByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean isUsernameExists(String username) {
        return userMapper.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean isEmailExists(String email) {
        return userMapper.existsByEmail(email);
    }

    /**
     * 检查手机号是否存在
     */
    public boolean isPhoneExists(String phone) {
        return userMapper.existsByPhone(phone);
    }

    /**
     * 获取用户详细信息（包含用户资料）
     */
    public UserInfoResponse getUserInfo(String userId) {
        User user = getUserById(userId);

        UserProfile userProfile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId));

        // 使用MapStruct进行对象转换和脱敏处理
        UserInfoResponse response;
        if (userProfile != null) {
            response = userConverter.userAndProfileToUserInfoResponse(user, userProfile);
        } else {
            response = userConverter.userToUserInfoResponse(user);
        }

        return response;
    }

    /**
     * 更新用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse updateUserInfo(String userId, UpdateUserInfoRequest request) {
        User user = getUserById(userId);

        // 更新用户基本信息
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            // 检查新用户名是否已存在
            if (isUsernameExists(request.getUsername())) {
                throw new BusinessException(ApiCode.USER_ALREADY_EXISTS, "用户名已被使用");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // 验证邮箱格式
            if (!ValidationUtil.isValidEmail(request.getEmail())) {
                throw new BusinessException(ApiCode.INVALID_EMAIL_FORMAT);
            }
            // 检查新邮箱是否已存在
            if (isEmailExists(request.getEmail())) {
                throw new BusinessException(ApiCode.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            // 验证手机号格式
            if (!ValidationUtil.isValidPhone(request.getPhone())) {
                throw new BusinessException(ApiCode.INVALID_PHONE_FORMAT);
            }
            // 检查新手机号是否已存在
            if (isPhoneExists(request.getPhone())) {
                throw new BusinessException(ApiCode.PHONE_ALREADY_EXISTS);
            }
            user.setPhone(request.getPhone());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 更新用户资料
        updateUserProfile(userId, request);

        log.info("用户信息更新成功: userId={}, username={}", userId, user.getUsername());
        return getUserInfo(userId);
    }

    /**
     * 获取用户列表（管理员功能）
     */
    public List<UserInfoResponse> getUserList(int page, int size, String status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .ne(User::getStatus, "deleted");

        if (status != null && !status.isEmpty()) {
            wrapper.eq(User::getStatus, status);
        }

        List<User> users = userMapper.selectList(wrapper);
        return users.stream()
                .map(userConverter::userToUserInfoResponse)
                .toList();
    }

    /**
     * 更新用户资料
     */
    private void updateUserProfile(String userId, UpdateUserInfoRequest request) {
        UserProfile userProfile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId));

        if (userProfile == null) {
            // 如果用户资料不存在，创建一个新的
            userProfile = new UserProfile();
            userProfile.setUserId(userId);
            userProfile.setCreatedAt(LocalDateTime.now());
        }

        // 更新用户资料字段
        if (request.getDisplayName() != null) {
            userProfile.setDisplayName(request.getDisplayName());
        }
        if (request.getAvatar() != null) {
            userProfile.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            userProfile.setBio(request.getBio());
        }
        if (request.getLanguage() != null) {
            userProfile.setLanguage(request.getLanguage());
        }
        if (request.getTimezone() != null) {
            userProfile.setTimezone(request.getTimezone());
        }
        if (request.getCurrency() != null) {
            userProfile.setCurrency(request.getCurrency());
        }
        if (request.getNotificationsEnabled() != null) {
            userProfile.setNotificationsEnabled(request.getNotificationsEnabled());
        }

        userProfile.setUpdatedAt(LocalDateTime.now());

        if (userProfile.getProfileId() == null) {
            userProfileMapper.insert(userProfile);
        } else {
            userProfileMapper.updateById(userProfile);
        }
    }
}