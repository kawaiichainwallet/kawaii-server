package com.kawaiichainwallet.common.core.exception;

import java.io.Serial;

/**
 * 加密解密异常
 */
public class CryptoException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
