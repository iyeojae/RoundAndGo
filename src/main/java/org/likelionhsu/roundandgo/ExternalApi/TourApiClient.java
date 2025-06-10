package org.likelionhsu.roundandgo.ExternalApi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Dto.DetailInfoDto;
import org.likelionhsu.roundandgo.Dto.TourApiGolfDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "detailInfo", key = "#contentId")
    public DetailInfoDto fetchDetailInfo(String contentId) {
        String path = "/detailInfo2";

        DetailInfoApiResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("serviceKey", apiKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "RoundAndGo")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", 28)
                        .queryParam("_type", "json")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DetailInfoApiResponse.class)
                .onErrorReturn(new DetailInfoApiResponse())
                .block();

        if (response == null || response.getResponse() == null ||
                response.getResponse().getBody() == null ||
                response.getResponse().getBody().getItems() == null ||
                response.getResponse().getBody().getItems().getItem() == null) {
            return new DetailInfoDto();
        }

        return response.getResponse().getBody().getItems().getItem().stream().findFirst().map(item -> {
            DetailInfoDto dto = new DetailInfoDto();
            dto.setFeeInfo(item.getInfotext());
            return dto;
        }).orElse(new DetailInfoDto());
    }

    @Data
    public static class DetailInfoApiResponse {
        private Response response;

        @Data
        public static class Response {
            private Body body;
        }

        @Data
        public static class Body {
            @JsonSetter(nulls = Nulls.SKIP)
            private Items items;
        }

        @Data
        public static class Items {
            @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
            private List<Item> item;
        }

        @Data
        public static class Item {
            @JsonProperty("infoname")
            private String infoname;

            @JsonProperty("infotext")
            private String infotext;
        }
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
