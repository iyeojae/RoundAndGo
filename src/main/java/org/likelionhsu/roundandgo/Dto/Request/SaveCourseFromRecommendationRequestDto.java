package org.likelionhsu.roundandgo.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaveCourseFromRecommendationRequestDto {
    private String courseName;
    private String description;
    private Boolean isPublic;
    private List<Long> recommendationIds; // 추천 코스 ID 리스트
}
