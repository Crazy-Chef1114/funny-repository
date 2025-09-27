package com.xzz.independent.exception;

import com.xzz.independent.enumeration.ErrorCode;
import lombok.Getter;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/25 18:05
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final String userMessage;

    public BusinessException(String errorCode, String message, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public BusinessException(ErrorCode errorCode, String userMessage) {
        this(errorCode.getCode(), errorCode.getDescription(), userMessage);
    }

}
