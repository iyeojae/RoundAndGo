package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Dto.Request.SaveCourseFromRecommendationRequestDto;
import org.likelionhsu.roundandgo.Dto.Request.SavedCourseRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.SavedCourseResponseDto;
import org.likelionhsu.roundandgo.Entity.*;
import org.likelionhsu.roundandgo.Repository.CourseRecommendationRepository;
import org.likelionhsu.roundandgo.Repository.GolfCourseRepository;
import org.likelionhsu.roundandgo.Repository.SavedCourseRepository;
import org.likelionhsu.roundandgo.Repository.ScheduleRepository;
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
    private final CourseRecommendationRepository courseRecommendationRepository;
    private final ScheduleRepository scheduleRepository;

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

    // 추천 코스 ID로 코스 저장
    public SavedCourseResponseDto saveCourseFromRecommendation(User user, SaveCourseFromRecommendationRequestDto request) {
        // 1. 추천 코스들을 조회하고 검증
        List<CourseRecommendation> recommendations = courseRecommendationRepository.findAllById(request.getRecommendationIds());

        if (recommendations.isEmpty()) {
            throw new RuntimeException("추천 코스를 찾을 수 없습니다.");
        }

        // 2. 첫 번째 추천 코스 기반으로 기본 정보 설정
        CourseRecommendation firstRecommendation = recommendations.get(0);

        // 3. SavedCourse 엔티티 생성
        SavedCourse savedCourse = SavedCourse.create(
                request.getCourseName(),
                request.getDescription(),
                firstRecommendation.getCourseType(),
                firstRecommendation.getStartDate(),
                firstRecommendation.getTravelDays(),
                request.getIsPublic(),
                user
        );

        // 4. 각 추천 코스를 SavedCourseDay로 변환하여 추가
        for (CourseRecommendation recommendation : recommendations) {
            SavedCourseDay courseDay = SavedCourseDay.create(
                    recommendation.getDayNumber(),
                    recommendation.getStartDate().plusDays(recommendation.getDayNumber() - 1),
                    recommendation.getTeeOffTime(),
                    recommendation.getGolfCourse()
            );

            // 5. 추천 장소들을 SavedCoursePlace로 변환하여 추가
            int visitOrder = 1;
            for (RecommendedPlace recommendedPlace : recommendation.getRecommendedPlaces()) {
                SavedCoursePlace place = SavedCoursePlace.create(
                        recommendedPlace.getType(),
                        recommendedPlace.getName(),
                        recommendedPlace.getAddress(),
                        recommendedPlace.getImageUrl(),
                        recommendedPlace.getDistanceKm(),
                        recommendedPlace.getMapx(),
                        recommendedPlace.getMapy(),
                        visitOrder++
                );
                courseDay.addPlace(place);
            }

            savedCourse.addCourseDay(courseDay);
        }

        SavedCourse saved = savedCourseRepository.save(savedCourse);

        // 6. 스케줄에도 자동으로 반영
        syncToSchedule(user, saved);

        return SavedCourseResponseDto.of(saved);
    }

    // 코스 수정 (없으면 새로 저장, 기존 코스와 다르면 업데이트)
    public SavedCourseResponseDto updateOrCreateCourseFromRecommendation(User user, Long courseId, SaveCourseFromRecommendationRequestDto request) {
        // 1. 기존 코스 조회 (없으면 null)
        SavedCourse existingCourse = savedCourseRepository.findByIdAndUser(courseId, user);

        if (existingCourse == null) {
            // 코스가 없으면 새로 생성
            return saveCourseFromRecommendation(user, request);
        }

        // 2. 추천 코스들을 조회하고 검증
        List<CourseRecommendation> recommendations = courseRecommendationRepository.findAllById(request.getRecommendationIds());

        if (recommendations.isEmpty()) {
            throw new RuntimeException("추천 코스를 찾을 수 없습니다.");
        }

        // 3. 기본 정보 업데이트
        CourseRecommendation firstRecommendation = recommendations.get(0);
        existingCourse.setCourseName(request.getCourseName());
        existingCourse.setDescription(request.getDescription());
        existingCourse.setCourseType(firstRecommendation.getCourseType());
        existingCourse.setStartDate(firstRecommendation.getStartDate());
        existingCourse.setTravelDays(firstRecommendation.getTravelDays());
        existingCourse.setIsPublic(request.getIsPublic());

        // 4. 기존 일차 정보 삭제 후 새로 추가
        existingCourse.getCourseDays().clear();

        // 5. 각 추천 코스를 SavedCourseDay로 변환하여 추가
        for (CourseRecommendation recommendation : recommendations) {
            SavedCourseDay courseDay = SavedCourseDay.create(
                    recommendation.getDayNumber(),
                    recommendation.getStartDate().plusDays(recommendation.getDayNumber() - 1),
                    recommendation.getTeeOffTime(),
                    recommendation.getGolfCourse()
            );

            // 6. 추천 장소들을 SavedCoursePlace로 변환하여 추가
            int visitOrder = 1;
            for (RecommendedPlace recommendedPlace : recommendation.getRecommendedPlaces()) {
                SavedCoursePlace place = SavedCoursePlace.create(
                        recommendedPlace.getType(),
                        recommendedPlace.getName(),
                        recommendedPlace.getAddress(),
                        recommendedPlace.getImageUrl(),
                        recommendedPlace.getDistanceKm(),
                        recommendedPlace.getMapx(),
                        recommendedPlace.getMapy(),
                        visitOrder++
                );
                courseDay.addPlace(place);
            }

            existingCourse.addCourseDay(courseDay);
        }

        SavedCourse updated = savedCourseRepository.save(existingCourse);

        // 7. 스케줄 업데이트
        syncToSchedule(user, updated);

        return SavedCourseResponseDto.of(updated);
    }

    // 코스를 스케줄에 반영하는 메서드
    private void syncToSchedule(User user, SavedCourse savedCourse) {
        // 기존 관련 스케줄 삭제 (코스 이름으로 찾아서)
        List<Schedule> existingSchedules = scheduleRepository.findByUserAndTitleContaining(user, savedCourse.getCourseName());
        scheduleRepository.deleteAll(existingSchedules);

        // 새로운 스케줄 생성
        for (SavedCourseDay courseDay : savedCourse.getCourseDays()) {
            // 골프장 스케줄
            Schedule golfSchedule = new Schedule();
            golfSchedule.setTitle(savedCourse.getCourseName() + " - " + courseDay.getGolfCourse().getName());
            golfSchedule.setStartDateTime(courseDay.getCourseDate().atTime(9, 0).toString());
            golfSchedule.setEndDateTime(courseDay.getCourseDate().atTime(17, 0).toString());
            golfSchedule.setUser(user);
            golfSchedule.setCategory("골프");
            golfSchedule.setLocation(courseDay.getGolfCourse().getName());
            scheduleRepository.save(golfSchedule);

            // 방문 장소들 스케줄
            for (SavedCoursePlace place : courseDay.getSavedPlaces()) {
                Schedule placeSchedule = new Schedule();
                placeSchedule.setTitle(savedCourse.getCourseName() + " - " + place.getName());
                placeSchedule.setStartDateTime(courseDay.getCourseDate().atTime(18, 0).plusHours(place.getVisitOrder() - 1).toString());
                placeSchedule.setEndDateTime(courseDay.getCourseDate().atTime(19, 0).plusHours(place.getVisitOrder() - 1).toString());
                placeSchedule.setUser(user);
                placeSchedule.setCategory(place.getType());
                placeSchedule.setLocation(place.getAddress());
                scheduleRepository.save(placeSchedule);
            }
        }
    }
}
