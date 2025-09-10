package org.likelionhsu.roundandgo.ExternalApi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Common.Config.OpenAiConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.likelionhsu.roundandgo.Dto.Api.OpenAiRequestDto;
import org.likelionhsu.roundandgo.Dto.Api.OpenAiResponseDto;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiApiClient {

    private final WebClient openAiWebClient;
    private final OpenAiConfig openAiConfig;

    public String generateCourseRecommendation(String userPrompt) {
        try {
            OpenAiRequestDto request = OpenAiRequestDto.builder()
                    .model(openAiConfig.getModel())
                    .maxTokens(openAiConfig.getMaxTokens())
                    .temperature(openAiConfig.getTemperature())
                    .messages(List.of(
                            OpenAiRequestDto.Message.builder()
                                    .role("system")
                                    .content("당신은 제주도 골프 여행 코스 추천 전문가입니다. 주어진 골프장, 숙소, 관광지, 음식점 정보를 바탕으로 최적의 여행 코스를 추천해주세요.")
                                    .build(),
                            OpenAiRequestDto.Message.builder()
                                    .role("user")
                                    .content(userPrompt)
                                    .build()
                    ))
                    .build();

            OpenAiResponseDto response = openAiWebClient
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAiResponseDto.class)
                    .block();

            if (response != null && !response.getChoices().isEmpty()) {
                return response.getChoices().getFirst().getMessage().getContent();
            }

            return "추천 코스를 생성할 수 없습니다.";

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: ", e);
            return "추천 코스 생성 중 오류가 발생했습니다.";
        }
    }
}
