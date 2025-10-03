package com.kawaiichainwallet.common.core.exception;

import com.kawaiichainwallet.common.core.enums.ApiCode;

import java.io.Serial;

/**
 * ID生成异常
 */
public class IdGenerationException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public IdGenerationException(String message) {
        super(ApiCode.INTERNAL_SERVER_ERROR, message);
    }

    public IdGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdGenerationException(ApiCode apiCode) {
        super(apiCode);
    }

    public IdGenerationException(ApiCode apiCode, String message) {
        super(apiCode, message);
    }
}
