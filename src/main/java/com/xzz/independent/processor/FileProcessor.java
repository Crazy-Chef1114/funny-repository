package com.xzz.independent.processor;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 文件处理器接口
 * @Author xzz_Cao
 * @Date 2025/9/25 10:19
 */
public interface FileProcessor {

    /**
     * 判断是否支持该文件类型
     */
    boolean supports(String filename);
    /**
     * 解析文件内容 - 需修改项：返回类型改为Mono，参数改为FilePart
     */
    Mono<List<Map<String, Object>>> parseFile(FilePart filePart);

    /**
     * 将解析的数据转换为适合AI分析的格式
     */
    String convertToAnalysisFormat(List<Map<String, Object>> data);

    /**
     * 获取文件类型
     */
    String getFileType();

}
