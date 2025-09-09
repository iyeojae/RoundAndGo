package org.likelionhsu.roundandgo.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "saved_course_place")
public class SavedCoursePlace {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // "food", "tour", "stay"

    @Column(nullable = false)
    private String name; // 장소명

    @Column(nullable = false)
    private String address; // 주소

    private String imageUrl; // 이미지 URL

    private Double distanceKm; // 골프장으로부터의 거리 (km)

    private String mapx; // 경도

    private String mapy; // 위도

    @Column(nullable = false)
    private Integer visitOrder; // 방문 순서 (1, 2, 3...)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_course_day_id", nullable = false)
    private SavedCourseDay savedCourseDay;

    // 정적 팩토리 메서드
    public static SavedCoursePlace create(
            String type,
            String name,
            String address,
            String imageUrl,
            Double distanceKm,
            String mapx,
            String mapy,
            Integer visitOrder) {

        SavedCoursePlace place = new SavedCoursePlace();
        place.type = type;
        place.name = name;
        place.address = address;
        place.imageUrl = imageUrl;
        place.distanceKm = distanceKm;
        place.mapx = mapx;
        place.mapy = mapy;
        place.visitOrder = visitOrder;

        return place;
    }
}
