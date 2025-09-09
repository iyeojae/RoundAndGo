package org.likelionhsu.roundandgo.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.likelionhsu.roundandgo.Entity.SavedCourse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SavedCourseResponseDto {

    private Long id;
    private String courseName;
    private String description;
    private String courseType;
    private String courseTypeLabel;
    private LocalDate startDate;
    private Integer travelDays;
    private Boolean isPublic;
    private String createdBy; // 코스 작성자
    private List<SavedCourseDayResponseDto> courseDays;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SavedCourseDayResponseDto {
        private Long id;
        private Integer dayNumber;
        private LocalDate courseDate;
        private LocalTime teeOffTime;
        private String golfCourseName;
        private String golfCourseAddress;
        private List<SavedCoursePlaceResponseDto> places;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SavedCoursePlaceResponseDto {
        private Long id;
        private String type;
        private String name;
        private String address;
        private String imageUrl;
        private Double distanceKm;
        private String mapx;
        private String mapy;
        private Integer visitOrder;
    }

    // 정적 팩토리 메서드
    public static SavedCourseResponseDto of(SavedCourse entity) {

        List<SavedCourseDayResponseDto> courseDayDtos = entity.getCourseDays().stream()
                .map(day -> SavedCourseDayResponseDto.builder()
                        .id(day.getId())
                        .dayNumber(day.getDayNumber())
                        .courseDate(day.getCourseDate())
                        .teeOffTime(day.getTeeOffTime())
                        .golfCourseName(day.getGolfCourse().getName())
                        .golfCourseAddress(day.getGolfCourse().getAddress())
                        .places(day.getSavedPlaces().stream()
                                .map(place -> SavedCoursePlaceResponseDto.builder()
                                        .id(place.getId())
                                        .type(place.getType())
                                        .name(place.getName())
                                        .address(place.getAddress())
                                        .imageUrl(place.getImageUrl())
                                        .distanceKm(place.getDistanceKm())
                                        .mapx(place.getMapx())
                                        .mapy(place.getMapy())
                                        .visitOrder(place.getVisitOrder())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return SavedCourseResponseDto.builder()
                .id(entity.getId())
                .courseName(entity.getCourseName())
                .description(entity.getDescription())
                .courseType(entity.getCourseType())
                .courseTypeLabel(resolveLabel(entity.getCourseType()))
                .startDate(entity.getStartDate())
                .travelDays(entity.getTravelDays())
                .isPublic(entity.getIsPublic())
                .createdBy(entity.getUser().getNickname())
                .courseDays(courseDayDtos)
                .build();
    }

    private static String resolveLabel(String courseType) {
        return switch (courseType) {
            case "luxury" -> "럭셔리 / 프리미엄";
            case "value" -> "가성비 / 실속";
            case "resort" -> "휴양 / 리조트";
            case "theme" -> "독특한 경험 / 테마";
            default -> "기타";
        };
    }
}
