package com.kawaiichainwallet.common.core.enums;

import lombok.Getter;

/**
 * API响应状态码枚举
 */
@Getter
public enum ApiCode {

    // 成功响应
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 服务端错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    // 业务错误 1xxx
    VALIDATION_ERROR(1001, "参数验证失败"),
    BUSINESS_ERROR(1002, "业务处理失败"),

    // 用户相关错误 2xxx
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_ALREADY_EXISTS(2002, "用户已存在"),
    INVALID_CREDENTIALS(2003, "用户名或密码错误"),
    ACCOUNT_LOCKED(2004, "账户已被锁定"),
    ACCOUNT_DISABLED(2005, "账户已被禁用"),
    PASSWORD_EXPIRED(2006, "密码已过期"),
    EMAIL_ALREADY_EXISTS(2007, "邮箱已被注册"),
    PHONE_ALREADY_EXISTS(2008, "手机号已被注册"),
    INVALID_EMAIL_FORMAT(2009, "邮箱格式不正确"),
    INVALID_PHONE_FORMAT(2010, "手机号格式不正确"),
    WEAK_PASSWORD(2011, "密码强度不足"),
    USER_INFO_FETCH_FAILED(2012, "获取用户信息失败"),
    BATCH_USER_FETCH_FAILED(2013, "批量获取用户信息失败"),
    USER_PAYMENT_PERMISSION_FETCH_FAILED(2014, "获取用户支付权限失败"),

    // 验证码相关错误 3xxx
    OTP_NOT_FOUND(3001, "验证码不存在"),
    OTP_EXPIRED(3002, "验证码已过期"),
    OTP_INVALID(3003, "验证码错误"),
    OTP_SEND_FAILED(3004, "验证码发送失败"),
    OTP_SEND_TOO_FREQUENT(3005, "验证码发送过于频繁"),
    OTP_VERIFY_TOO_MANY_ATTEMPTS(3006, "验证码验证次数过多"),

    // Token相关错误 4xxx
    TOKEN_INVALID(4001, "Token无效"),
    INVALID_TOKEN(4001, "Token无效"), // 别名，保持兼容性
    TOKEN_EXPIRED(4002, "Token已过期"),
    TOKEN_MALFORMED(4003, "Token格式错误"),
    REFRESH_TOKEN_INVALID(4004, "刷新Token无效"),
    INTERNAL_TOKEN_INVALID(4005, "无效的内部调用Token"),
    TOKEN_REVOKE_FAILED(4006, "Token撤销失败"),

    // 钱包相关错误 5xxx
    WALLET_NOT_FOUND(5001, "钱包不存在"),
    WALLET_ALREADY_EXISTS(5002, "钱包已存在"),
    INSUFFICIENT_BALANCE(5003, "余额不足"),
    INVALID_ADDRESS(5004, "地址格式错误"),
    TRANSACTION_FAILED(5005, "交易失败"),

    // 支付相关错误 6xxx
    PAYMENT_ORDER_NOT_FOUND(6001, "支付订单不存在"),
    PAYMENT_AMOUNT_INVALID(6002, "支付金额无效"),
    PAYMENT_EXPIRED(6003, "支付已过期"),
    PAYMENT_ALREADY_PAID(6004, "订单已支付");

    private final Integer code;
    private final String message;

    ApiCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}