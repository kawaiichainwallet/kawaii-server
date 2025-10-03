package com.kawaiichainwallet.common.core.exception;

import com.kawaiichainwallet.common.core.enums.ApiCode;

import java.io.Serial;

/**
 * 加密解密异常
 */
public class CryptoException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CryptoException(String message) {
        super(ApiCode.INTERNAL_SERVER_ERROR, message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoException(ApiCode apiCode) {
        super(apiCode);
    }

    public CryptoException(ApiCode apiCode, String message) {
        super(apiCode, message);
    }
}
