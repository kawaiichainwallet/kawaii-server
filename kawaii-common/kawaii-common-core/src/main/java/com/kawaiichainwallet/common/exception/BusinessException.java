package com.kawaiichainwallet.common.exception;

import com.kawaiichainwallet.common.enums.ApiCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1295240355417138709L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    public BusinessException(ApiCode apiCode) {
        super(apiCode.getMessage());
        this.code = apiCode.getCode();
        this.message = apiCode.getMessage();
    }

    public BusinessException(ApiCode apiCode, String message) {
        super(message);
        this.code = apiCode.getCode();
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        super(message);
        this.code = ApiCode.BUSINESS_ERROR.getCode();
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ApiCode.BUSINESS_ERROR.getCode();
        this.message = message;
    }
}