package com.nutcracker.server.exception;

import java.io.Serial;

/**
 * 自定义异常类
 *
 * @author 胡桃夹子
 * @date 2020/2/24 18:08
 */
public class ProviderException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2248546206040115304L;

    private String errorCode;

    public ProviderException() {

    }

    public ProviderException(String message) {
        this.errorCode = errorCode;
    }

    public ProviderException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ProviderException(Throwable caused) {
        super(caused);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
