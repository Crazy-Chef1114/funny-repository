package com.xzz.independent.enumeration;

import lombok.Getter;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/25 18:11
 */
@Getter
public enum ErrorCode {

    FILE_EMPTY("FILE_001", "文件为空"),
    FILE_TOO_LARGE("FILE_002", "文件大小超出限制"),
    UNSUPPORTED_FILE_TYPE("FILE_003", "不支持的文件类型"),
    FILE_PARSE_ERROR("FILE_004", "文件解析失败"),
    AI_SERVICE_UNAVAILABLE("AI_001", "AI服务不可用"),
    AI_INVALID_RESPONSE("AI_002", "AI响应格式错误"),
    INVALID_INPUT_PARAMETER("VALID_001", "输入参数无效");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
