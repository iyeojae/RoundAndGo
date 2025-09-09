package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Dto.Request.SavedCourseRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.SavedCourseResponseDto;
import org.likelionhsu.roundandgo.Entity.*;
import org.likelionhsu.roundandgo.Repository.GolfCourseRepository;
import org.likelionhsu.roundandgo.Repository.SavedCourseRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedCourseService {

    private final SavedCourseRepository savedCourseRepository;
    private final GolfCourseRepository golfCourseRepository;

    // 코스 저장
    public SavedCourseResponseDto saveCourse(User user, SavedCourseRequestDto request) {
        // 1. SavedCourse 엔티티 생성
        SavedCourse savedCourse = SavedCourse.create(
                request.getCourseName(),
                request.getDescription(),
                request.getCourseType(),
                request.getStartDate(),
                request.getTravelDays(),
                request.getIsPublic(),
                user
        );

        // 2. 각 일차별 정보 추가
        for (SavedCourseRequestDto.SavedCourseDayDto dayDto : request.getCourseDays()) {
            GolfCourse golfCourse = golfCourseRepository.findById(dayDto.getGolfCourseId())
                    .orElseThrow(() -> new RuntimeException("골프장을 찾을 수 없습니다: " + dayDto.getGolfCourseId()));

            SavedCourseDay courseDay = SavedCourseDay.create(
                    dayDto.getDayNumber(),
                    dayDto.getCourseDate(),
                    dayDto.getTeeOffTime(),
                    golfCourse
            );

            // 3. 각 일차별 방문 장소 추가
            for (SavedCourseRequestDto.SavedCoursePlaceDto placeDto : dayDto.getPlaces()) {
                SavedCoursePlace place = SavedCoursePlace.create(
                        placeDto.getType(),
                        placeDto.getName(),
                        placeDto.getAddress(),
                        placeDto.getImageUrl(),
                        placeDto.getDistanceKm(),
                        placeDto.getMapx(),
                        placeDto.getMapy(),
                        placeDto.getVisitOrder()
                );
                courseDay.addPlace(place);
            }

            savedCourse.addCourseDay(courseDay);
        }

        SavedCourse saved = savedCourseRepository.save(savedCourse);
        return SavedCourseResponseDto.of(saved);
    }

    // 내 저장된 코스 목록 조회
    @Transactional(readOnly = true)
    public List<SavedCourseResponseDto> getMyCourses(User user) {
        return savedCourseRepository.findByUserOrderByIdDesc(user).stream()
                .map(SavedCourseResponseDto::of)
                .toList();
    }

    // 공개된 코스 목록 조회
    @Transactional(readOnly = true)
    public List<SavedCourseResponseDto> getPublicCourses() {
        return savedCourseRepository.findByIsPublicTrueOrderByIdDesc().stream()
                .map(SavedCourseResponseDto::of)
                .toList();
    }

    // 코스 타입별 공개 코스 조회
    @Transactional(readOnly = true)
    public List<SavedCourseResponseDto> getPublicCoursesByType(String courseType) {
        return savedCourseRepository.findByIsPublicTrueAndCourseTypeOrderByIdDesc(courseType).stream()
                .map(SavedCourseResponseDto::of)
                .toList();
    }

    // 단건 코스 조회
    @Transactional(readOnly = true)
    public SavedCourseResponseDto getCourse(Long courseId) {
        SavedCourse course = savedCourseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("저장된 코스를 찾을 수 없습니다."));
        return SavedCourseResponseDto.of(course);
    }

    // 코스 수정
    public SavedCourseResponseDto updateCourse(User user, Long courseId, SavedCourseRequestDto request) {
        SavedCourse course = savedCourseRepository.findByIdAndUser(courseId, user);
        if (course == null) {
            throw new AccessDeniedException("해당 코스를 수정할 권한이 없습니다.");
        }

        // 기본 정보 수정
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setCourseType(request.getCourseType());
        course.setStartDate(request.getStartDate());
        course.setTravelDays(request.getTravelDays());
        course.setIsPublic(request.getIsPublic());

        // 기존 일차 정보 삭제 후 새로 추가
        course.getCourseDays().clear();

        for (SavedCourseRequestDto.SavedCourseDayDto dayDto : request.getCourseDays()) {
            GolfCourse golfCourse = golfCourseRepository.findById(dayDto.getGolfCourseId())
                    .orElseThrow(() -> new RuntimeException("골프장을 찾을 수 없습니다: " + dayDto.getGolfCourseId()));

            SavedCourseDay courseDay = SavedCourseDay.create(
                    dayDto.getDayNumber(),
                    dayDto.getCourseDate(),
                    dayDto.getTeeOffTime(),
                    golfCourse
            );

            for (SavedCourseRequestDto.SavedCoursePlaceDto placeDto : dayDto.getPlaces()) {
                SavedCoursePlace place = SavedCoursePlace.create(
                        placeDto.getType(),
                        placeDto.getName(),
                        placeDto.getAddress(),
                        placeDto.getImageUrl(),
                        placeDto.getDistanceKm(),
                        placeDto.getMapx(),
                        placeDto.getMapy(),
                        placeDto.getVisitOrder()
                );
                courseDay.addPlace(place);
            }

            course.addCourseDay(courseDay);
        }

        SavedCourse updated = savedCourseRepository.save(course);
        return SavedCourseResponseDto.of(updated);
    }

    // 코스 삭제
    public void deleteCourse(User user, Long courseId) {
        SavedCourse course = savedCourseRepository.findByIdAndUser(courseId, user);
        if (course == null) {
            throw new AccessDeniedException("해당 코스를 삭제할 권한이 없습니다.");
        }
        savedCourseRepository.delete(course);
    }
}
