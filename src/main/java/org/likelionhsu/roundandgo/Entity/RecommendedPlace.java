package org.likelionhsu.roundandgo.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.likelionhsu.roundandgo.Dto.RecommendedPlaceDto;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendedPlace {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // food / tour / stay
    private String name;
    private String address;
    private String imageUrl;

    private Double distanceKm;

    private String mapx;
    private String mapy;

    @ManyToOne(fetch = FetchType.LAZY)
    private CourseRecommendation courseRecommendation;

    public static RecommendedPlace of(RecommendedPlaceDto dto, CourseRecommendation parent) {
        RecommendedPlace place = new RecommendedPlace();
        place.type = dto.getType();
        place.name = dto.getName();
        place.address = dto.getAddress();
        place.imageUrl = dto.getImageUrl();
        place.mapx = dto.getMapx();
        place.mapy = dto.getMapy();
        place.distanceKm = dto.getDistanceKm();
        place.courseRecommendation = parent;
        return place;
    }
}