package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.CourseRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRecommendationRepository extends JpaRepository<CourseRecommendation, Long> {
}