package org.likelionhsu.roundandgo.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.likelionhsu.roundandgo.Dto.Api.RecommendedPlaceDto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseRecommendation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseType; // "luxury", "value", "resort", "theme"
    private String courseTypeLabel;

    private LocalTime teeOffTime;
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private GolfCourse golfCourse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Optional: if you want to track which user created the recommendation

    @OneToMany(mappedBy = "courseRecommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecommendationOrder> recommendationOrders = new ArrayList<>();

    @OneToMany(mappedBy = "courseRecommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecommendedPlace> recommendedPlaces = new ArrayList<>();

    public static CourseRecommendation create(
            GolfCourse golfCourse,
            String courseType,
            LocalTime teeOffTime,
            LocalTime endTime,
            List<RecommendedPlaceDto> placeDtos,
            User user) {

        CourseRecommendation course = new CourseRecommendation();
        course.golfCourse = golfCourse;
        course.courseType = courseType;
        course.courseTypeLabel = resolveLabel(courseType);
        course.teeOffTime = teeOffTime;
        course.endTime = endTime;
        for (String orderType : determineOrder(endTime)) {
            RecommendationOrder order = new RecommendationOrder();
            order.setType(orderType);
            order.setCourseRecommendation(course);
            course.recommendationOrders.add(order);
        }
        course.user = user; // Optional, if you want to associate with a user

        for (RecommendedPlaceDto dto : placeDtos) {
            if (dto != null) {
                course.recommendedPlaces.add(RecommendedPlace.of(dto, course));
            }
        }

        return course;
    }

    private static String resolveLabel(String type) {
        return switch (type) {
            case "luxury" -> "럭셔리 / 프리미엄";
            case "value" -> "가성비 / 실속";
            case "resort" -> "휴양 / 리조트";
            case "theme" -> "독특한 경험 / 테마";
            default -> "기타";
        };
    }

    private static List<String> determineOrder(LocalTime endTime) {
        if (endTime.isBefore(LocalTime.of(13, 0))) return List.of("food", "tour", "stay");
        if (endTime.isBefore(LocalTime.of(17, 0))) return List.of("tour", "food", "stay");
        return List.of("food", "stay");
    }
}
