package com.xzz.independent.exception;

import com.xzz.independent.enumeration.ErrorCode;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/25 18:07
 */
public class AIServiceException extends BusinessException {

    public AIServiceException(ErrorCode errorCode, String userMessage) {
        super(errorCode, userMessage);
    }

}
