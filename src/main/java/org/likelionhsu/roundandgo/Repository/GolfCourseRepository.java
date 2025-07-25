package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.GolfCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GolfCourseRepository extends JpaRepository<GolfCourse, Long> {
    Optional<GolfCourse> findByContentId(String contentId);
    List<GolfCourse> findAll();
    List<GolfCourse> findByAddressContainingIgnoreCase(String address);
}