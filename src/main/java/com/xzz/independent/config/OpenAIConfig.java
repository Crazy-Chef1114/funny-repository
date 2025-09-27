package com.xzz.independent.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/25 11:44
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.openai")
public class OpenAIConfig {

    private String key;
    private String url;
    private Duration timeout = Duration.ofSeconds(30);

    @Bean
    public WebClient openAiWebClient() {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + key)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}
