package com.kawaiichainwallet.user.entity;

import com.kawaiichainwallet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 用户资料实体类 - 对应 user_profiles 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserProfile extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 7725232293474192226L;

    /**
     * 资料ID
     */
    private String profileId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 名字
     */
    private String firstName;

    /**
     * 姓氏
     */
    private String lastName;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 性别 (male, female, other, prefer_not_say)
     */
    private String gender;

    /**
     * 国家代码 (ISO 3166-1 alpha-3)
     */
    private String country;

    /**
     * 省/州
     */
    private String stateProvince;

    /**
     * 城市
     */
    private String city;

    /**
     * 邮政编码
     */
    private String postalCode;

    /**
     * 地址行1
     */
    private String addressLine1;

    /**
     * 地址行2
     */
    private String addressLine2;

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