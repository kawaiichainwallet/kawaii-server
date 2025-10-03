package com.kawaiichainwallet.common.core.exception;

import java.io.Serial;

/**
 * JWT处理异常
 */
public class JwtException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public JwtException(String message) {
        super(message);
    }

    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
