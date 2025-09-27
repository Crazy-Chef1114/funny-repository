package com.xzz.independent.service.impl;

import com.xzz.independent.ai.UniversalAIService;
import com.xzz.independent.factory.FileProcessorFactory;
import com.xzz.independent.processor.FileProcessor;
import com.xzz.independent.service.FileAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/25 19:36
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileAnalysisServiceImpl implements FileAnalysisService {

    private final UniversalAIService universalAIService;
    private final FileProcessorFactory fileProcessorFactory;

    @Override
    public Mono<String> analyzeExcelFile(Map<String, Object> analysisRequest) {
        try {
            // 需修改项：从FilePart转换为具体的文件处理器调用
            FilePart filePart = (FilePart) analysisRequest.get("filePart");
            String analysisType = (String) analysisRequest.get("analysisType");
            String additionalInstructions = (String) analysisRequest.get("additionalInstructions");

            FileProcessor processor = fileProcessorFactory.getProcessor(filePart);

            // 需修改项：使用flatMap处理Mono返回值
            return processor.parseFile(filePart)
                    .flatMap(excelData -> {
                        String formattedData = processor.convertToAnalysisFormat(excelData);
                        String prompt = buildAnalysisPrompt(formattedData, analysisType, additionalInstructions);
                        return universalAIService.analyzeData(prompt);
                    });

        } catch (Exception e) {
            log.error("文件分析服务执行失败", e);
            return Mono.error(e);
        }
    }

    private String buildAnalysisPrompt(String data, String analysisType, String instructions) {
        return String.format("""
            请对以下数据进行%s分析：
            %s
            %s
            请提供专业的分析结果和建议。
            """, analysisType, data,
                instructions != null ? "额外要求：" + instructions : "");
    }

}
