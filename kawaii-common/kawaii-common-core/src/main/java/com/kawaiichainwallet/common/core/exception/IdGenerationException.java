package com.kawaiichainwallet.common.core.exception;

import java.io.Serial;

/**
 * ID生成异常
 */
public class IdGenerationException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public IdGenerationException(String message) {
        super(message);
    }

    public IdGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
