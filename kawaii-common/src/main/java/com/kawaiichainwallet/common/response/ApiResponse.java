package com.kawaiichainwallet.common.response;

import com.kawaiichainwallet.common.enums.ApiCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装
 */
@Data
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 4385759500555397916L;

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳（Unix毫秒时间戳）
     */
    private Long timestamp;

    /**
     * 请求追踪ID
     */
    private String traceId;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public ApiResponse(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ApiCode.SUCCESS.getCode(), ApiCode.SUCCESS.getMessage());
    }

    /**
     * 成功响应，带数据
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiCode.SUCCESS.getCode(), ApiCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应，自定义消息
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ApiCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(ApiCode apiCode) {
        return new ApiResponse<>(apiCode.getCode(), apiCode.getMessage());
    }

    /**
     * 错误响应，自定义消息
     */
    public static <T> ApiResponse<T> error(ApiCode apiCode, String message) {
        return new ApiResponse<>(apiCode.getCode(), message);
    }

    /**
     * 错误响应，自定义状态码和消息
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message);
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return ApiCode.SUCCESS.getCode().equals(this.code);
    }
}