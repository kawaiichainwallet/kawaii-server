package com.kawaiichainwallet.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kawaiichainwallet.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 用户资料实体类 - 对应 user_profiles 表
 * 注意：user_profiles 表中没有 created_by 和 updated_by 字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_profiles")
public class UserProfile extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 7725232293474192226L;

    // 排除 BaseEntity 中在数据库表中不存在的字段
    @TableField(exist = false)
    private String createdBy;

    @TableField(exist = false)
    private String updatedBy;

    /**
     * 资料ID (使用Leaf分布式ID生成器)
     */
    @TableId(value = "profile_id", type = IdType.INPUT)
    private Long profileId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

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
    @TableField("avatar_url")
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