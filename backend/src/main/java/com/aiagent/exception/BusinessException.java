package com.aiagent.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author ego
 * @date 2025-11-29
 */
@Getter
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}

