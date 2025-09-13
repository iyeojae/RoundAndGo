package org.likelionhsu.roundandgo.ExternalApi;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Dto.Api.TourApiGolfDto;
import org.likelionhsu.roundandgo.Dto.Api.TourItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TourApiClient {

    private final WebClient webClient;

    @Value("${culture-api.key}")
    private String apiKey; // TourAPI도 동일 키 사용

    public TourApiClient(@Qualifier("tourWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<TourApiGolfDto> fetchGolfList() {
        String path = "/areaBasedList2";

        TourApiResponseGolf response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("serviceKey", apiKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "RoundAndGo")
                        .queryParam("contentTypeId", 28)
                        .queryParam("cat1", "A03")
                        .queryParam("cat2", "A0302")
                        .queryParam("cat3", "A03020700")
                        .queryParam("numOfRows", 1000)
                        .queryParam("_type", "json")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TourApiResponseGolf.class)
                .block();

        if (response == null || response.getResponse() == null ||
                response.getResponse().getBody() == null ||
                response.getResponse().getBody().getItems() == null) {
            return List.of();
        }

        return response.getResponse().getBody().getItems().getItem();
    }

    public List<TourItem> fetchByContentTypes(int areaCode, int sigunguCode, List<Integer> contentTypeIds) {
        List<TourItem> result = new ArrayList<>();

        for (int contentTypeId : contentTypeIds) {
            try {
                TourApiResponseGeneral response = webClient.get()
                        .uri(uriBuilder -> {
                            var builder = uriBuilder
                                    .path("/areaBasedList2")
                                    .queryParam("serviceKey", apiKey)
                                    .queryParam("MobileOS", "ETC")
                                    .queryParam("MobileApp", "RoundAndGo")
                                    .queryParam("_type", "json")
                                    .queryParam("numOfRows", 100)
                                    .queryParam("areaCode", areaCode)
                                    .queryParam("contentTypeId", contentTypeId);

                            // sigunguCode가 0이 아닐 때만 시군구 코드 파라미터 추가
                            if (sigunguCode > 0) {
                                builder.queryParam("sigunguCode", sigunguCode);
                            }

                            return builder.build();
                        })
                        .retrieve()
                        .bodyToMono(TourApiResponseGeneral.class)
                        .onErrorReturn(new TourApiResponseGeneral())
                        .block();

                if (response != null && response.getResponse() != null &&
                        response.getResponse().getBody() != null &&
                        response.getResponse().getBody().getItems() != null &&
                        response.getResponse().getBody().getItems().getItem() != null) {
                    result.addAll(response.getResponse().getBody().getItems().getItem());
                }
            } catch (Exception e) {
                // JSON 파싱 오류나 기타 오류 발생 시 해당 contentTypeId는 건너뛰고 계속 진행
                log.warn("Tour API 호출 중 오류 발생 - contentTypeId: {}, areaCode: {}, sigunguCode: {}, error: {}",
                        contentTypeId, areaCode, sigunguCode, e.getMessage());
            }
        }

        return result;
    }

    public List<TourItem> fetchNearbyItems(double mapX, double mapY, List<Integer> contentTypeId) {
        int[] radiusSteps = {5000, 10000, 20000};
        String path = "/locationBasedList2";

        for (int radius : radiusSteps) {
            TourApiResponseGeneral response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("serviceKey", apiKey)
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "RoundAndGo")
                            .queryParam("_type", "json")
                            .queryParam("numOfRows", 100)
                            .queryParam("arrange", "E") // 거리순 정렬
                            .queryParam("mapX", mapX)
                            .queryParam("mapY", mapY)
                            .queryParam("radius", radius)
                            .queryParam("contentTypeId", contentTypeId)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(TourApiResponseGeneral.class)
                    .block();

            if (response != null && response.getResponse() != null &&
                    response.getResponse().getBody() != null &&
                    response.getResponse().getBody().getItems() != null &&
                    response.getResponse().getBody().getItems().getItem() != null &&
                    !response.getResponse().getBody().getItems().getItem().isEmpty()) {
                return response.getResponse().getBody().getItems().getItem();
            }
        }

        return List.of(); // 모든 반경에서 결과가 없을 경우
    }

    @Data
    public static class TourApiResponseGolf {
        private ResponseGolf response;

        @Data
        public static class ResponseGolf {
            private BodyGolf body;
        }

        @Data
        public static class BodyGolf {
            private ItemsGolf items;
        }

        @Data
        public static class ItemsGolf {
            private List<TourApiGolfDto> item;
        }
    }

    @Data
    public static class TourApiResponseGeneral {
        private ResponseGeneral response;

        @Data
        public static class ResponseGeneral {
            private BodyGeneral body;
        }

        @Data
        public static class BodyGeneral {
            private ItemsGeneral items;
        }

        @Data
        public static class ItemsGeneral {
            private List<TourItem> item;
        }
    }


}
