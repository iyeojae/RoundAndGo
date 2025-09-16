package org.likelionhsu.roundandgo.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.likelionhsu.roundandgo.Dto.Api.RecommendedPlaceDto;
import org.likelionhsu.roundandgo.Entity.CourseRecommendation;
import org.likelionhsu.roundandgo.Entity.RecommendationOrder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseRecommendationResponseDto {
    private Long id;
    private String courseTypeLabel;
    private String golfCourseName;

    // 기본 시간 정보
    private String teeOffTime; // 티오프 시간
    private String golfEndTime; // 골프 종료 시간
    private String estimatedEndTime; // 하위 호환성을 위해 유지

    private List<String> recommendationOrder; // ["food", "tour", "stay"]
    private List<RecommendedPlaceDto> recommendedPlaces; // 이제 각 장소마다 시간 정보 포함

    // 2일 연속 골프장 방문을 위한 추가 필드
    private LocalDate startDate; // 여행 시작 날짜
    private Integer travelDays; // 여행 기간 (1일 또는 2일)
    private Integer dayNumber; // 몇일차인지 (1일차, 2일차)

    public static CourseRecommendationResponseDto of(CourseRecommendation entity) {
        // 시간 계산을 위한 기본 변수들
        LocalTime teeOff = entity.getTeeOffTime();
        LocalTime golfEnd = entity.getEndTime();
        LocalTime currentTime = golfEnd.plusMinutes(30); // 골프 종료 30분 후부터 시작

        List<RecommendedPlaceDto> placeDtos = new ArrayList<>();

        // 1. 먼저 골프장 정보를 첫 번째로 추가
        RecommendedPlaceDto golfPlaceDto = RecommendedPlaceDto.builder()
                .type("golf")
                .name(entity.getGolfCourse().getName())
                .address(entity.getGolfCourse().getAddress())
                .imageUrl(null) // 골프장 이미지는 별도 처리 필요시 추가
                .distanceKm(0.0)
                .mapx(String.valueOf(entity.getGolfCourse().getLongitude()))
                .mapy(String.valueOf(entity.getGolfCourse().getLatitude()))
                // 골프 시간 정보
                .startTime(teeOff.toString())
                .endTime(golfEnd.toString())
                .duration("4시간 30분")
                .timeLabel("골프 라운딩")
                .build();

        placeDtos.add(golfPlaceDto);

        // 2. 추천 장소들을 순서대로 처리하면서 시간 정보 추가
        for (var place : entity.getRecommendedPlaces()) {
            LocalTime startTime = currentTime;
            LocalTime endTime;
            String duration;
            String timeLabel;

            // 장소 유형별 시간 설정
            switch (place.getType()) {
                case "food" -> {
                    endTime = startTime.plusHours(1).plusMinutes(30);
                    duration = "1시간 30분";
                    timeLabel = "점심 시간";
                }
                case "tour" -> {
                    endTime = startTime.plusHours(2);
                    duration = "2시간";
                    timeLabel = "관광 시간";
                }
                case "stay" -> {
                    endTime = LocalTime.of(23, 59);
                    duration = "숙박";
                    timeLabel = "숙소 도착";
                }
                default -> {
                    endTime = startTime.plusHours(1);
                    duration = "1시간";
                    timeLabel = "기타 활동";
                }
            }

            RecommendedPlaceDto placeDto = RecommendedPlaceDto.builder()
                    .type(place.getType())
                    .name(place.getName())
                    .address(place.getAddress())
                    .imageUrl(place.getImageUrl())
                    .distanceKm(place.getDistanceKm())
                    .mapx(place.getMapx())
                    .mapy(place.getMapy())
                    // 시간 정보 추가
                    .startTime(startTime.toString())
                    .endTime(endTime.toString())
                    .duration(duration)
                    .timeLabel(timeLabel)
                    .build();

            placeDtos.add(placeDto);

            // 다음 장소를 위한 시간 업데이트 (숙소가 아닌 경우만)
            if (!place.getType().equals("stay")) {
                currentTime = endTime.plusMinutes(30); // 이동 시간 30분 추가
            }
        }

        List<String> orderTypes = entity.getRecommendationOrders().stream()
                .map(RecommendationOrder::getType)
                .toList();

        return CourseRecommendationResponseDto.builder()
                .id(entity.getId())
                .courseTypeLabel(entity.getCourseTypeLabel())
                .golfCourseName(entity.getGolfCourse().getName())
                .teeOffTime(teeOff.toString())
                .golfEndTime(golfEnd.toString())
                .estimatedEndTime(golfEnd.toString()) // 하위 호환성을 위해 유지
                .recommendationOrder(orderTypes)
                .recommendedPlaces(placeDtos) // 이제 골프장 + 추천 장소들 모두 포함
                .startDate(entity.getStartDate())
                .travelDays(entity.getTravelDays())
                .dayNumber(entity.getDayNumber())
                .build();
    }
}
