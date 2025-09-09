package org.likelionhsu.roundandgo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRecommendationRequestDto {
    private Long golfCourseId;
    private String teeOffTime; // "08:00"
    private String courseType; // "luxury", "value", "resort", "theme"

    // 2일 연속 골프장 방문을 위한 추가 필드
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate; // 여행 시작 날짜
    private Integer travelDays; // 여행 기간 (1일 또는 2일)
    private List<Long> golfCourseIds; // 2일차 골프장을 위한 리스트 (1일차, 2일차)
    private List<String> teeOffTimes; // 각 날짜별 티오프 시간
}
