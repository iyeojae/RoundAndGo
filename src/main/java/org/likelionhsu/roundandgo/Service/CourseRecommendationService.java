package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Common.ScheduleColor;
import org.likelionhsu.roundandgo.Dto.Request.ScheduleRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.CourseRecommendationResponseDto;
import org.likelionhsu.roundandgo.Dto.Api.RecommendedPlaceDto;
import org.likelionhsu.roundandgo.Dto.Api.TourItem;
import org.likelionhsu.roundandgo.Entity.CourseRecommendation;
import org.likelionhsu.roundandgo.Entity.GolfCourse;
import org.likelionhsu.roundandgo.Entity.RecommendedPlace;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.ExternalApi.TourApiClient;
import org.likelionhsu.roundandgo.Mapper.CourseTypeMapper;
import org.likelionhsu.roundandgo.Repository.CourseRecommendationRepository;
import org.likelionhsu.roundandgo.Repository.GolfCourseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseRecommendationService {

    private final GolfCourseRepository golfCourseRepository;
    private final CourseRecommendationRepository courseRecommendationRepository;
    private final TourApiClient tourApiClient;
    private final ScheduleService scheduleService;

    public CourseRecommendationResponseDto createRecommendation(User user, Long golfCourseId, String teeOffTime, String courseType) {

        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장 정보를 찾을 수 없습니다."));

        LocalTime teeOff = LocalTime.parse(teeOffTime);
        LocalTime endTime = teeOff.plusHours(4).plusMinutes(30);
        List<String> order = getRecommendationOrder(endTime);

        List<RecommendedPlaceDto> foodList = getPlaces(golfCourse, 39);
        List<RecommendedPlaceDto> tourList = getPlaces(golfCourse, 12);
        List<RecommendedPlaceDto> stayList = getFilteredStays(golfCourse, courseType);

        RecommendedPlaceDto food = foodList.stream().findFirst().orElse(null);
        RecommendedPlaceDto tour = tourList.stream().findFirst().orElse(null);
        RecommendedPlaceDto stay = stayList.stream().findFirst().orElse(null);

        CourseRecommendation saved = courseRecommendationRepository.save(
                CourseRecommendation.create(golfCourse, courseType, teeOff, endTime, List.of(food, tour, stay), user)
        );

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        createSchedulesForRecommendation(user, golfCourse, date, saved);

        return CourseRecommendationResponseDto.of(saved);
    }

    public List<CourseRecommendationResponseDto> getRecommendationsByUser(User user) {
        return courseRecommendationRepository.findByUser(user).stream()
                .map(CourseRecommendationResponseDto::of)
                .toList();
    }

    public CourseRecommendationResponseDto getRecommendation(Long id) {
        CourseRecommendation course = courseRecommendationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("추천 코스를 찾을 수 없습니다."));
        return CourseRecommendationResponseDto.of(course);
    }

    public CourseRecommendationResponseDto updateRecommendation(Long id, Long golfCourseId, String teeOffTime, String courseType, User user) {
        CourseRecommendation course = courseRecommendationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("추천 코스를 찾을 수 없습니다."));

        if (!course.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 추천을 수정할 권한이 없습니다.");
        }

        LocalTime teeOff = LocalTime.parse(teeOffTime);
        LocalTime endTime = teeOff.plusHours(4).plusMinutes(30);

        course.getRecommendedPlaces().clear();
        course.setTeeOffTime(teeOff);
        course.setEndTime(endTime);
        course.setCourseType(courseType);
        course.setCourseTypeLabel(resolveLabel(courseType));
        course.setRecommendationOrder(getRecommendationOrder(endTime));

        // 골프장 정보 유지 or 수정
        GolfCourse golfCourse = course.getGolfCourse();
        if (golfCourseId != null && !golfCourseId.equals(golfCourse.getId())) {
            golfCourse = golfCourseRepository.findById(golfCourseId)
                    .orElseThrow(() -> new RuntimeException("골프장 정보를 찾을 수 없습니다."));
            course.setGolfCourse(golfCourse);
        }

        // 장소 재조회 및 등록
        RecommendedPlaceDto food = getPlaces(golfCourse, 39).stream().findFirst().orElse(null);
        RecommendedPlaceDto tour = getPlaces(golfCourse, 12).stream().findFirst().orElse(null);
        RecommendedPlaceDto stay = getFilteredStays(golfCourse, courseType).stream().findFirst().orElse(null);

        if (food != null) course.getRecommendedPlaces().add(RecommendedPlace.of(food, course));
        if (tour != null) course.getRecommendedPlaces().add(RecommendedPlace.of(tour, course));
        if (stay != null) course.getRecommendedPlaces().add(RecommendedPlace.of(stay, course));

        courseRecommendationRepository.save(course);
        return CourseRecommendationResponseDto.of(course);
    }

    private List<RecommendedPlaceDto> getPlaces(GolfCourse golfCourse, int contentTypeId) {
        return tourApiClient.fetchNearbyItems(golfCourse.getLongitude(), golfCourse.getLatitude(), List.of(contentTypeId))
                .stream()
                .map(this::toRecommendedPlace)
                .toList();
    }

    private List<RecommendedPlaceDto> getFilteredStays(GolfCourse golfCourse, String courseType) {
        List<String> cat3Codes = CourseTypeMapper.getCat3Codes(courseType);
        return tourApiClient.fetchNearbyItems(golfCourse.getLongitude(), golfCourse.getLatitude(), List.of(32))
                .stream()
                .filter(item -> cat3Codes.contains(item.getCat3()))
                .map(this::toRecommendedPlace)
                .toList();
    }

    private List<String> getRecommendationOrder(LocalTime endTime) {
        if (endTime.isBefore(LocalTime.of(13, 0))) return List.of("food", "tour", "stay");
        if (endTime.isBefore(LocalTime.of(17, 0))) return List.of("tour", "food", "stay");
        return List.of("food", "stay");
    }

    private RecommendedPlaceDto toRecommendedPlace(TourItem item) {
        return RecommendedPlaceDto.builder()
                .type(resolveType(item.getContenttypeid()))
                .name(item.getTitle())
                .address(item.getAddr1())
                .imageUrl(item.getFirstimage())
                .distanceKm(0.0)
                .mapx(item.getMapx())
                .mapy(item.getMapy())
                .build();
    }

    private String resolveType(int contentTypeId) {
        return switch (contentTypeId) {
            case 12 -> "tour";
            case 32 -> "stay";
            case 39 -> "food";
            default -> "unknown";
        };
    }

    public static String resolveLabel(String courseType) {
        return switch (courseType) {
            case "luxury" -> "럭셔리 / 프리미엄";
            case "value" -> "가성비 / 실속";
            case "resort" -> "휴양 / 리조트";
            case "theme" -> "독특한 경험 / 테마";
            default -> "기타";
        };
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CommonResponse.builder()
                        .statusCode(403)
                        .msg(e.getMessage())
                        .build());
    }

    private void createSchedulesForRecommendation(
            User user,
            GolfCourse golfCourse,
            String date,  // 예시: "2025-08-15"
            CourseRecommendation courseRecommendation
    ) {
        // (1) 골프장 일정
        ScheduleRequestDto golfSchedule = new ScheduleRequestDto();
        golfSchedule.setTitle("[라운딩] " + golfCourse.getName());
        golfSchedule.setAllDay(true);
        golfSchedule.setCategory("라운딩");
        golfSchedule.setLocation(golfCourse.getAddress());
        golfSchedule.setColor(ScheduleColor.GREEN);
        String startDateTime = date + "T00:00:00";
        String endDateTime = date + "T23:59:59";
        scheduleService.createSchedule(user, golfSchedule, startDateTime , endDateTime);

        // (2) 추천 장소(음식점, 관광지, 숙소) 일정
        List<RecommendedPlace> recommendedPlaces = courseRecommendation.getRecommendedPlaces();
        for (RecommendedPlace place : recommendedPlaces) {
            ScheduleRequestDto placeSchedule = new ScheduleRequestDto();
            placeSchedule.setTitle("[추천] " + place.getName());
            placeSchedule.setAllDay(true);
            placeSchedule.setCategory(place.getType());
            placeSchedule.setLocation(place.getAddress());
            placeSchedule.setColor(ScheduleColor.BLUE);
            scheduleService.createSchedule(user, placeSchedule, startDateTime, endDateTime);
        }
    }
}


