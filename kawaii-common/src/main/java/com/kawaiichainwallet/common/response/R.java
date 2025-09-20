package com.kawaiichainwallet.common.response;

import com.kawaiichainwallet.common.enums.ApiCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一响应结果封装
 * @param <T> 响应数据类型
 */
@Data
public class R<T> {

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 请求追踪ID
     */
    private String traceId;

    /**
     * 是否成功
     */
    private Boolean success;

    public R() {
        this.timestamp = LocalDateTime.now();
    }

    public R(Integer code, String msg, T data) {
        this();
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.success = code == 200;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> R<T> success() {
        return new R<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带消息）
     */
    public static <T> R<T> success(String msg) {
        return new R<>(200, msg, null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> success(T data) {
        return new R<>(200, "操作成功", data);
    }

    /**
     * 成功响应（带数据和消息）
     */
    public static <T> R<T> success(T data, String msg) {
        return new R<>(200, msg, data);
    }

    /**
     * 失败响应
     */
    public static <T> R<T> error() {
        return new R<>(500, "操作失败", null);
    }

    /**
     * 失败响应（带消息）
     */
    public static <T> R<T> error(String msg) {
        return new R<>(500, msg, null);
    }

    /**
     * 失败响应（带状态码和消息）
     */
    public static <T> R<T> error(Integer code, String msg) {
        return new R<>(code, msg, null);
    }

    /**
     * 失败响应（带状态码、消息和数据）
     */
    public static <T> R<T> error(Integer code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    /**
     * 基于ApiCode的成功响应
     */
    public static <T> R<T> success(ApiCode apiCode) {
        return new R<>(apiCode.getCode(), apiCode.getMessage(), null);
    }

    /**
     * 基于ApiCode的成功响应（带数据）
     */
    public static <T> R<T> success(ApiCode apiCode, T data) {
        return new R<>(apiCode.getCode(), apiCode.getMessage(), data);
    }

    /**
     * 基于ApiCode的失败响应
     */
    public static <T> R<T> error(ApiCode apiCode) {
        return new R<>(apiCode.getCode(), apiCode.getMessage(), null);
    }

    /**
     * 基于ApiCode的失败响应（带数据）
     */
    public static <T> R<T> error(ApiCode apiCode, T data) {
        return new R<>(apiCode.getCode(), apiCode.getMessage(), data);
    }

    /**
     * 设置追踪ID
     */
    public R<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(this.success);
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}