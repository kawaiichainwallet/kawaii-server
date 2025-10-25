package com.kawaiichainwallet.user.dto;

import lombok.Data;

/**
 * 更新用户信息请求
 */
@Data
public class UpdateUserInfoRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 语言偏好
     */
    private String language;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 货币偏好
     */
    private String currency;

    /**
     * 是否启用通知
     */
    private Boolean notificationsEnabled;
}