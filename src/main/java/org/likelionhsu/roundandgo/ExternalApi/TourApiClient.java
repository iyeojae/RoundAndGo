package org.likelionhsu.roundandgo.ExternalApi;

import lombok.Data;
import org.likelionhsu.roundandgo.Dto.TourApiGolfDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class TourApiClient {

    private final WebClient webClient;

    @Value("${culture-api.key}")
    private String apiKey; // TourAPI도 동일 키 사용

    public TourApiClient(@Qualifier("tourWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<TourApiGolfDto> fetchGolfList() {
        String path = "/areaBasedList2";

        TourApiResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("serviceKey", apiKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "RoundAndGo")
                        .queryParam("contentTypeId", 28)    // 레포츠
                        .queryParam("cat1", "A03")          // 레포츠
                        .queryParam("cat2", "A0302")        // 육상 레포츠
                        .queryParam("cat3", "A03020700")    // 골프
                        .queryParam("numOfRows", 1000)       // 100개씩 보기
                        .queryParam("_type", "json")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TourApiResponse.class)
                .block();

        if (response == null || response.getResponse() == null ||
                response.getResponse().getBody() == null ||
                response.getResponse().getBody().getItems() == null) {
            return List.of();
        }

        return response.getResponse().getBody().getItems().getItem();
    }

    @Data
    public static class TourApiResponse {
        private Response response;

        @Data
        public static class Response {
            private Body body;
        }

        @Data
        public static class Body {
            private Items items;
        }

        @Data
        public static class Items {
            private List<TourApiGolfDto> item;
        }
    }
}
