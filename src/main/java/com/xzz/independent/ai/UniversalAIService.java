package com.xzz.independent.ai;

import com.xzz.independent.enumeration.ErrorCode;
import com.xzz.independent.exception.AIServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/25 9:11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniversalAIService {

    private final WebClient openAiWebClient;

    public Mono<String> analyzeData(String prompt) {
        Map<String, Object> requestBody = buildAIRequest(prompt);

        return openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new AIServiceException(
                                ErrorCode.AI_SERVICE_UNAVAILABLE,
                                "AI服务异常: " + response.statusCode()
                        )))
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .map(this::extractContent)
                .doOnError(error -> log.error("AI服务调用失败", error));
    }

    private Map<String, Object> buildAIRequest(String prompt) {
        return Map.of(
                "model", "gpt-3.5-turbo",
                "messages", new Object[]{
                        Map.of("role", "system", "content", "你是专业的数据分析专家"),
                        Map.of("role", "user", "content", prompt)
                },
                "temperature", 0.3,
                "max_tokens", 2000
        );
    }

    private boolean isRetryableException(Throwable throwable) {
        return throwable instanceof WebClientResponseException ||
                throwable instanceof java.util.concurrent.TimeoutException;
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        try {
            var choices = (java.util.List<Map<String, Object>>) response.get("choices");
            var message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            throw new AIServiceException(ErrorCode.AI_INVALID_RESPONSE, "AI响应解析失败");
        }
    }

}
