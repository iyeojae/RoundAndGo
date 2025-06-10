package org.likelionhsu.roundandgo.Common.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CultureWebClientConfig {

    @Bean(name = "cultureWebClient")
    public WebClient cultureWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.odcloud.kr/api")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                        .build())
                .build();
    }
}
