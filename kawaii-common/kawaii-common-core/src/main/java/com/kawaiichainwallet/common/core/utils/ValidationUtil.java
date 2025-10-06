package com.kawaiichainwallet.common.core.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;

import java.util.regex.Pattern;

/**
 * 验证工具类
 */
public class ValidationUtil {

    // 手机号正则表达式（中国大陆）
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // 密码强度正则表达式（8-20位，至少包含一个大写字母、一个小写字母、一个数字）
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,20}$"
    );

    // 用户名正则表达式（3-20位，字母数字下划线）
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    // 数字验证码正则表达式（4-8位数字）
    private static final Pattern NUMERIC_CODE_PATTERN = Pattern.compile("^\\d{4,8}$");

    /**
     * 验证手机号是否有效
     */
    public static boolean isValidPhone(String phone) {
        return CharSequenceUtil.isNotBlank(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证邮箱是否有效
     */
    public static boolean isValidEmail(String email) {
        return CharSequenceUtil.isNotBlank(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证密码强度是否符合要求
     */
    public static boolean isValidPassword(String password) {
        return CharSequenceUtil.isNotBlank(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 验证用户名是否有效
     */
    public static boolean isValidUsername(String username) {
        return CharSequenceUtil.isNotBlank(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 验证数字验证码是否有效
     */
    public static boolean isValidNumericCode(String code) {
        return CharSequenceUtil.isNotBlank(code) && NUMERIC_CODE_PATTERN.matcher(code).matches();
    }

    /**
     * 验证字符串是否不为空且长度在指定范围内
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (CharSequenceUtil.isBlank(str)) {
            return false;
        }
        int length = str.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * 获取手机号验证错误信息
     */
    public static String getPhoneValidationMessage() {
        return "手机号格式不正确，请输入有效的11位手机号";
    }

    /**
     * 获取邮箱验证错误信息
     */
    public static String getEmailValidationMessage() {
        return "邮箱格式不正确，请输入有效的邮箱地址";
    }

    /**
     * 获取密码验证错误信息
     */
    public static String getPasswordValidationMessage() {
        return "密码必须为8-20位，且包含至少一个大写字母、一个小写字母和一个数字";
    }

    /**
     * 获取用户名验证错误信息
     */
    public static String getUsernameValidationMessage() {
        return "用户名必须为3-20位字母、数字或下划线";
    }

    /**
     * 获取验证码验证错误信息
     */
    public static String getCodeValidationMessage() {
        return "验证码必须为4-8位数字";
    }

    /**
     * 脱敏手机号（保留前3位和后4位）
     */
    public static String maskPhone(String phone) {
        if (!isValidPhone(phone)) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 脱敏邮箱（保留前2位和@域名）
     */
    public static String maskEmail(String email) {
        if (!isValidEmail(email)) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return email;
        }
        String prefix = email.substring(0, 2);
        String suffix = email.substring(atIndex);
        return prefix + "****" + suffix;
    }

    /**
     * 脱敏敏感信息（通用方法）
     */
    public static String maskSensitiveInfo(String info) {
        if (CharSequenceUtil.isBlank(info)) {
            return info;
        }

        if (info.contains("@")) {
            return maskEmail(info);
        } else if (isValidPhone(info)) {
            return maskPhone(info);
        } else {
            // 对于其他信息，保留前2位和后2位
            if (info.length() <= 4) {
                return "****";
            }
            return info.substring(0, 2) + "****" + info.substring(info.length() - 2);
        }
    }

    /**
     * 检查是否为有效的UUID格式
     */
    public static boolean isValidUUID(String uuid) {
        if (CharSequenceUtil.isBlank(uuid)) {
            return false;
        }
        try {
            java.util.UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}