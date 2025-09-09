package org.likelionhsu.roundandgo.Common.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties(prefix = "openai.api")
@Getter
@Setter
@Slf4j
public class OpenAiConfig {

    private String key;
    private String url;
    private String model;
    private Integer maxTokens;
    private Double temperature;

    @Bean
    public WebClient openAiWebClient() {
        log.info("OpenAI WebClient 생성 중 - URL: {}, Key 존재여부: {}", url, key != null && !key.isEmpty());

        if (url == null || url.isEmpty()) {
            log.error("OpenAI API URL이 설정되지 않았습니다.");
            url = "https://api.openai.com/v1"; // 기본값 설정 (baseUrl이므로 /chat/completions 제외)
        }

        if (key == null || key.isEmpty()) {
            log.error("OpenAI API Key가 설정되지 않았습니다.");
            key = "dummy-key"; // 더미 키 설정
        }

        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + key)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
