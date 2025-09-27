package com.xzz.independent.service;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/25 19:34
 */
public interface FileAnalysisService {

    Mono<String> analyzeExcelFile(Map<String, Object> analysisRequest);

}
