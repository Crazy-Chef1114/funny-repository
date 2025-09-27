package com.xzz.independent.factory;

import com.xzz.independent.enumeration.ErrorCode;
import com.xzz.independent.exception.FileProcessingException;
import com.xzz.independent.processor.FileProcessor;
import com.xzz.independent.processor.LargeFileProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/25 10:23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileProcessorFactory {

    private final List<FileProcessor> fileProcessors;

    public FileProcessor getProcessor(FilePart filePart) {
        String filename = filePart.filename();
        FileProcessor baseProcessor = fileProcessors.stream()
                .filter(processor -> processor.supports(filename))
                .findFirst()
                .orElseThrow(() -> new FileProcessingException(
                        ErrorCode.UNSUPPORTED_FILE_TYPE,
                        "不支持的文件类型: " + filename
                ));

        // 新增：如果是大型Excel文件，返回专门的大型文件处理器
        if (filename.toLowerCase().endsWith(".xlsx")) {
            return fileProcessors.stream()
                    .filter(processor -> processor instanceof LargeFileProcessor)
                    .findFirst()
                    .orElse(baseProcessor); // 如果没有大型处理器，回退到基础处理器
        }

        return baseProcessor;
    }

}
