package org.likelionhsu.roundandgo.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CourseRecommendationRequestDto {
    private Long golfCourseId;
    private String teeOffTime; // "08:00"
    private String courseType; // "luxury", "value", "resort", "theme"
}
