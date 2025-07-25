package org.likelionhsu.roundandgo.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.likelionhsu.roundandgo.Dto.Api.RecommendedPlaceDto;
import org.likelionhsu.roundandgo.Entity.CourseRecommendation;
import org.likelionhsu.roundandgo.Entity.RecommendationOrder;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseRecommendationResponseDto {
    private Long id;
    private String courseTypeLabel;
    private String golfCourseName;
    private String estimatedEndTime;
    private List<String> recommendationOrder; // ["food", "tour", "stay"]
    private List<RecommendedPlaceDto> recommendedPlaces;

    public static CourseRecommendationResponseDto of(CourseRecommendation entity) {
        List<RecommendedPlaceDto> placeDtos = entity.getRecommendedPlaces().stream()
                .map(place -> RecommendedPlaceDto.builder()
                        .type(place.getType())
                        .name(place.getName())
                        .address(place.getAddress())
                        .imageUrl(place.getImageUrl())
                        .distanceKm(place.getDistanceKm())
                        .mapx(place.getMapx())
                        .mapy(place.getMapy())
                        .build())
                .toList();

        // 무한 순환 원인: recommendationOrders 엔티티를 그대로 담아서!
        // List<RecommendationOrder> orders = entity.getRecommendationOrders();

        // ➡️ 이렇게 "type"만 리스트로 변환!
        List<String> orderTypes = entity.getRecommendationOrders().stream()
                .map(RecommendationOrder::getType)
                .toList();

        return CourseRecommendationResponseDto.builder()
                .id(entity.getId())
                .courseTypeLabel(entity.getCourseTypeLabel())
                .golfCourseName(entity.getGolfCourse().getName())
                .estimatedEndTime(entity.getEndTime().toString())
                .recommendationOrder(orderTypes)  // 무한반복 해결!
                .recommendedPlaces(placeDtos)
                .build();
    }

}
