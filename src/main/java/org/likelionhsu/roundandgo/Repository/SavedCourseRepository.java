package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.SavedCourse;
import org.likelionhsu.roundandgo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedCourseRepository extends JpaRepository<SavedCourse, Long> {

    // 사용자별 저장된 코스 조회
    List<SavedCourse> findByUserOrderByIdDesc(User user);

    // 공개된 코스 조회 (다른 사용자들이 볼 수 있는 코스)
    List<SavedCourse> findByIsPublicTrueOrderByIdDesc();

    // 코스 타입별 공개 코스 조회
    List<SavedCourse> findByIsPublicTrueAndCourseTypeOrderByIdDesc(String courseType);

    // 사용자가 저장한 코스 중 특정 코스 조회 (권한 체크용)
    @Query("SELECT sc FROM SavedCourse sc WHERE sc.id = :courseId AND sc.user = :user")
    SavedCourse findByIdAndUser(@Param("courseId") Long courseId, @Param("user") User user);
}
