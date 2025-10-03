package com.kawaiichainwallet.common.core.exception;

import com.kawaiichainwallet.common.core.enums.ApiCode;

import java.io.Serial;

/**
 * JWT处理异常
 */
public class JwtException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public JwtException(String message) {
        super(ApiCode.TOKEN_INVALID, message);
    }

    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtException(ApiCode apiCode) {
        super(apiCode);
    }

    public JwtException(ApiCode apiCode, String message) {
        super(apiCode, message);
    }
}
