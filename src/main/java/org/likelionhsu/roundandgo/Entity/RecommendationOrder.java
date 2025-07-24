package org.likelionhsu.roundandgo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter @Getter
@Table(name = "recommendation_order")
public class RecommendationOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "food", "tour", "stay"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_recommendation_id")
    private CourseRecommendation courseRecommendation;
}
