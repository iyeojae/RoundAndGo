package org.likelionhsu.roundandgo.ExternalApi;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Dto.CultureApiResponse;
import org.likelionhsu.roundandgo.Dto.CultureGolfDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CultureApiClient {

    private final WebClient webClient;

    @Value("${culture-api.key}")
    private String apiKey;

    public CultureApiClient(@Qualifier("cultureWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Cacheable(value = "cultureInfo", key = "#name")
    public CultureGolfDto findByName(String name) {
        String path = "/15118920/v1/uddi:0e5b12d2-1cc8-4caf-ba96-c2c7d1ef8d83";


        CultureApiResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("page", 1)
                        .queryParam("perPage", 1000)
                        .queryParam("returnType", "JSON")
                        .queryParam("serviceKey", apiKey) // encoding은 WebClient가 해줌
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CultureApiResponse.class)
                .block();

        if (response == null || response.getData() == null) return null;

        return response.getData().stream()
                .filter(item -> item.getGolfCourseName() != null && item.getGolfCourseName().contains(name))
                .findFirst()
                .map(item -> {
                    CultureGolfDto dto = new CultureGolfDto();
                    dto.setCourseType(item.getCourseType());
                    dto.setCourseLength(item.getCourseLength());
                    dto.setHoleCount(item.getHoleCount());
                    dto.setTotalArea(item.getTotalArea());
                    return dto;
                })
                .orElse(null);
    }
}
