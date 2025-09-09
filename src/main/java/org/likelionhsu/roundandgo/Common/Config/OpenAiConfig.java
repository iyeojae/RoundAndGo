package org.likelionhsu.roundandgo.Common.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConfigurationProperties(prefix = "openai.api")
@Getter
@Setter
public class OpenAiConfig {

    private String key;
    private String url;
    private String model;
    private Integer maxTokens;
    private Double temperature;

    @Bean
    public WebClient openAiWebClient() {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + key)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
