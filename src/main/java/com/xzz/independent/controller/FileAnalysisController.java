package com.xzz.independent.controller;

import com.xzz.independent.dto.response.ApiResponse;
import com.xzz.independent.service.FileAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/25 9:13
 */
@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class FileAnalysisController {

    private final FileAnalysisService fileAnalysisService;

    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ApiResponse<String>>> analyzeExcelFile(
            @RequestPart("file") FilePart filePart,
            @RequestParam("analysisType") String analysisType,
            @RequestParam(value = "additionalInstructions", required = false) String additionalInstructions) {

        log.info("接收Excel分析请求，文件名: {}, 分析类型: {}", filePart.filename(), analysisType);

        // 新增：文件大小验证（50MB限制）
        return filePart.content()
                .collectList()
                .flatMap(dataBuffers -> {
                    long fileSize = dataBuffers.stream()
                            .mapToInt(dataBuffer -> dataBuffer.readableByteCount())
                            .sum();

                    if (fileSize > 50 * 1024 * 1024) {
                        return Mono.just(ResponseEntity.badRequest()
                                .body(ApiResponse.error("文件大小不能超过50MB")));
                    }

                    // 文件大小验证通过，继续处理
                    Map<String, Object> analysisRequest = new HashMap<>();
                    analysisRequest.put("filePart", filePart);
                    analysisRequest.put("analysisType", analysisType);
                    analysisRequest.put("additionalInstructions", additionalInstructions);

                    return fileAnalysisService.analyzeExcelFile(analysisRequest)
                            .map(result -> ResponseEntity.ok(ApiResponse.success(result)))
                            .onErrorResume(error -> {
                                log.error("Excel文件分析失败", error);
                                return Mono.just(ResponseEntity.badRequest()
                                        .body(ApiResponse.error("分析失败: " + error.getMessage())));
                            });
                });
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("服务运行正常"));
    }

}
