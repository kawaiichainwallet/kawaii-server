package com.kawaiichainwallet.common.core.exception;

import java.io.Serial;

/**
 * 限流异常
 */
public class RateLimitException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
