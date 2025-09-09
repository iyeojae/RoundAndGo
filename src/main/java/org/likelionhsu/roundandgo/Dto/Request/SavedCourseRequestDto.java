package org.likelionhsu.roundandgo.Dto.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
public class SavedCourseRequestDto {

    private String courseName; // 코스 이름
    private String description; // 코스 설명
    private String courseType; // "luxury", "value", "resort", "theme"
    private LocalDate startDate; // 여행 시작 날짜
    private Integer travelDays; // 여행 기간
    private Boolean isPublic; // 공개 여부

    private List<SavedCourseDayDto> courseDays; // 각 일차별 정보

    @Data
    @NoArgsConstructor
    public static class SavedCourseDayDto {
        private Integer dayNumber; // 몇일차
        private LocalDate courseDate; // 해당 일차 날짜
        private LocalTime teeOffTime; // 티오프 시간
        private Long golfCourseId; // 골프장 ID
        private List<SavedCoursePlaceDto> places; // 방문할 장소들
    }

    @Data
    @NoArgsConstructor
    public static class SavedCoursePlaceDto {
        private String type; // "food", "tour", "stay"
        private String name;
        private String address;
        private String imageUrl;
        private Double distanceKm;
        private String mapx;
        private String mapy;
        private Integer visitOrder; // 방문 순서
    }
}
