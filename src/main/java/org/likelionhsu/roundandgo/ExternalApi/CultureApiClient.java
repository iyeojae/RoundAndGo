package org.likelionhsu.roundandgo.ExternalApi;

import org.likelionhsu.roundandgo.Dto.Api.CultureGolfDto;
import org.likelionhsu.roundandgo.Dto.Response.CultureApiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
public class CultureApiClient {

    private final WebClient webClient;

    @Value("${culture-api.key}")
    private String apiKey;

    public CultureApiClient(@Qualifier("cultureWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Cacheable(value = "cultureInfo", key = "#addr")
    public CultureGolfDto findByAddress(String addr, String golfName) {

        String normalizedTarget = normalize(addr);

        // 지역이 TourAPI는 앞에 시도군구를 안붙혀서 좀 복잡함 자세히 확인

        System.out.println("Normalized Target Address: " + normalizedTarget);

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

        // 1. 주소 기반 매칭 시도
        return response.getData().stream()
                .filter(item -> normalize(item.getAddr()).contains(normalizedTarget))
                .findFirst()
                .map(item -> toDto(item))
                // 2. 주소 매칭 실패 시, 골프장명(타이틀)로도 시도
                .orElseGet(() -> {
                    if (golfName == null) return null;
                    return response.getData().stream()
                            .filter(item -> isGolfNameMatch(item.getTitle(), golfName))  // ← 여기!
                            .findFirst()
                            .map(item -> toDto(item))
                            .orElse(null);
                });
    }

    // 공통 변환 메서드
    private CultureGolfDto toDto(CultureApiResponse.Item item) {
        CultureGolfDto dto = new CultureGolfDto();
        dto.setCourseType(item.getCourseType());
        dto.setHoleCount(item.getHoleCount());
        dto.setTotalArea(item.getTotalArea());
        return dto;
    }
    private static final String[] PROVINCES = {
            "경상남도", "경기도", "경상북도", "전북특별자치도", "충청북도",
            "대구광역시", "전라남도", "충청남도", "부산광역시",
            "제주특별자치도", "인천광역시", "강원특별자치도", "대전광역시",
            "세종특별자치시", "울산광역시", "서울특별시", "세종시", "서울시",
            "대전", "광주광역시", "부산", "인천시", "강원도"
    };

    private String normalize(String input) {
        if (input == null) return "";

        // 시/도 명칭 제거
        String result = input;
        for (String province : PROVINCES) {
            result = result.replace(province, "");
        }

        result = result.replaceAll("\\s+", "") // 모든 공백 제거
                .replaceAll("[()]", "") // 괄호 제거
                .toLowerCase();         // 소문자 통일
        return result;
    }

    private String normalizeGolfName(String name) {
        if (name == null) return "";
        // 1. 괄호 및 괄호 안 내용 제거
        String result = name.replaceAll("\\(.*?\\)", "");
        // 2. 공백/특수문자/소문자 통일
        result = result.replaceAll("\\s+", "")
                .replaceAll("[()]", "")
                .toLowerCase();
        return result;
    }

    private boolean isGolfNameMatch(String name1, String name2) {
        if (name1 == null || name2 == null) return false;

        String n1 = normalizeGolfName(name1);
        String n2 = normalizeGolfName(name2);

        // 1. 원본 비교
        if (n1.contains(n2)) return true;
        // 2. CC → 컨트리클럽 비교
        if (normalizeGolfName(name1.replace("CC", "컨트리클럽")).contains(normalizeGolfName(name2.replace("CC", "컨트리클럽")))) return true;
        // 3. 컨트리클럽 → CC 비교
        if (normalizeGolfName(name1.replace("컨트리클럽", "CC")).contains(normalizeGolfName(name2.replace("컨트리클럽", "CC")))) return true;

        // (추가: country club 변환도 여기에...)

        return false;
    }
}
