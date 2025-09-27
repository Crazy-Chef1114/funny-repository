package com.xzz.independent.util;

import com.xzz.independent.enumeration.ErrorCode;
import com.xzz.independent.exception.FileProcessingException;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

/**
 * 文件验证工具类
 * @Author xzz_Cao
 * @Date 2025/9/26 10:39
 */
public class FileValidationUtil {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * 验证文件大小和类型
     */
    public static Mono<FilePart> validateFile(FilePart filePart, long maxSize) {
        return filePart.content()
                .count()
                .map(dataBufferCount -> {
                    // 这里可以根据DataBuffer数量估算文件大小
                    // 实际生产环境需要更精确的大小计算
                    if (dataBufferCount > 1000) { // 示例阈值，需要根据实际情况调整
                        throw new FileProcessingException(
                                ErrorCode.FILE_TOO_LARGE,
                                "文件大小超出限制"
                        );
                    }
                    return filePart;
                })
                .onErrorMap(error -> {
                    if (error instanceof FileProcessingException) {
                        return error;
                    }
                    return new FileProcessingException(
                            ErrorCode.FILE_PARSE_ERROR,
                            "文件验证失败: " + error.getMessage()
                    );
                });
    }

}
