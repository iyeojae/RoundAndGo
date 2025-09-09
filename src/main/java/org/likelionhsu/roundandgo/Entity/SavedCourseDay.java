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
@Table(name = "saved_course_day")
public class SavedCourseDay {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer dayNumber; // 몇일차인지 (1일차, 2일차)

    @Column(nullable = false)
    private LocalDate courseDate; // 해당 일차의 날짜

    @Column(nullable = false)
    private LocalTime teeOffTime; // 티오프 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "golf_course_id", nullable = false)
    private GolfCourse golfCourse; // 해당 일차의 골프장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_course_id", nullable = false)
    private SavedCourse savedCourse;

    @OneToMany(mappedBy = "savedCourseDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavedCoursePlace> savedPlaces = new ArrayList<>();

    // 정적 팩토리 메서드
    public static SavedCourseDay create(
            Integer dayNumber,
            LocalDate courseDate,
            LocalTime teeOffTime,
            GolfCourse golfCourse) {

        SavedCourseDay courseDay = new SavedCourseDay();
        courseDay.dayNumber = dayNumber;
        courseDay.courseDate = courseDate;
        courseDay.teeOffTime = teeOffTime;
        courseDay.golfCourse = golfCourse;

        return courseDay;
    }

    // 장소 추가
    public void addPlace(SavedCoursePlace place) {
        this.savedPlaces.add(place);
        place.setSavedCourseDay(this);
    }
}
