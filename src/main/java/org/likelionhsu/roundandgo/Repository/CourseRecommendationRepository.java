package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.CourseRecommendation;
import org.likelionhsu.roundandgo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRecommendationRepository extends JpaRepository<CourseRecommendation, Long> {

    List<CourseRecommendation> findByUser(User user);
}