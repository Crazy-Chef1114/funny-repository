package com.xzz.independent.exception;

import com.xzz.independent.enumeration.ErrorCode;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/25 18:06
 */
public class FileProcessingException extends BusinessException{

    public FileProcessingException(ErrorCode errorCode, String userMessage) {
        super(errorCode, userMessage);
    }

}
