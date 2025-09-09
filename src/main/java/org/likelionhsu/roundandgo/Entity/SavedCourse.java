package org.likelionhsu.roundandgo.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "saved_course")
public class SavedCourse {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String courseName; // 사용자가 지정한 코스 이름

    @Column(length = 500)
    private String description; // 코스 설명

    @Column(nullable = false)
    private String courseType; // "luxury", "value", "resort", "theme"

    @Column(nullable = false)
    private LocalDate startDate; // 여행 시작 날짜

    @Column(nullable = false)
    private Integer travelDays; // 여행 기간 (1일 또는 2일)

    @Column(nullable = false)
    private Boolean isPublic = false; // 공개 여부 (다른 사용자가 볼 수 있는지)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 코스를 저장한 사용자

    @OneToMany(mappedBy = "savedCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavedCourseDay> courseDays = new ArrayList<>();

    // 정적 팩토리 메서드
    public static SavedCourse create(
            String courseName,
            String description,
            String courseType,
            LocalDate startDate,
            Integer travelDays,
            Boolean isPublic,
            User user) {

        SavedCourse savedCourse = new SavedCourse();
        savedCourse.courseName = courseName;
        savedCourse.description = description;
        savedCourse.courseType = courseType;
        savedCourse.startDate = startDate;
        savedCourse.travelDays = travelDays;
        savedCourse.isPublic = isPublic != null ? isPublic : false;
        savedCourse.user = user;

        return savedCourse;
    }

    // 코스 일차 추가
    public void addCourseDay(SavedCourseDay courseDay) {
        this.courseDays.add(courseDay);
        courseDay.setSavedCourse(this);
    }
}
