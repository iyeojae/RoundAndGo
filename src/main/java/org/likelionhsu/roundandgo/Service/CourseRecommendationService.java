package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Dto.CourseRecommendationRequestDto;
import org.likelionhsu.roundandgo.Dto.CourseRecommendationResponseDto;
import org.likelionhsu.roundandgo.Dto.RecommendedPlaceDto;
import org.likelionhsu.roundandgo.Dto.TourItem;
import org.likelionhsu.roundandgo.Entity.CourseRecommendation;
import org.likelionhsu.roundandgo.Entity.GolfCourse;
import org.likelionhsu.roundandgo.Entity.RecommendedPlace;
import org.likelionhsu.roundandgo.ExternalApi.TourApiClient;
import org.likelionhsu.roundandgo.Mapper.CourseTypeMapper;
import org.likelionhsu.roundandgo.Repository.CourseRecommendationRepository;
import org.likelionhsu.roundandgo.Repository.GolfCourseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseRecommendationService {

    private final GolfCourseRepository golfCourseRepository;
    private final CourseRecommendationRepository courseRecommendationRepository;

    private final TourApiClient tourApiClient; // 이미 구현된 WebClient 호출 클래스

    public CourseRecommendationResponseDto createRecommendation(Long golfCourseId,
                                                               String teeOffTime,
                                                               String courseType) {

        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장 정보를 찾을 수 없습니다."));

        // 종료 시간 계산
        LocalTime teeOff = LocalTime.parse(teeOffTime);
        LocalTime endTime = teeOff.plusHours(4).plusMinutes(30);

        // 추천 순서 결정
        List<String> order = getRecommendationOrder(endTime);

        // 음식점 (contentTypeId = 39)
        List<RecommendedPlaceDto> foodList = tourApiClient.fetchNearbyItems(
                        golfCourse.getLongitude(), golfCourse.getLatitude(), List.of(39))
                .stream()
                .map(this::toRecommendedPlace)
                .toList();

        // 관광지 (contentTypeId = 12)
        List<RecommendedPlaceDto> tourList = tourApiClient.fetchNearbyItems(
                        golfCourse.getLongitude(), golfCourse.getLatitude(), List.of(12))
                .stream()
                .map(this::toRecommendedPlace)
                .toList();

        // 숙소 (contentTypeId = 32 + cat3 필터)
        List<String> cat3Codes = CourseTypeMapper.getCat3Codes(courseType);

        List<RecommendedPlaceDto> stayList = tourApiClient.fetchNearbyItems(
                        golfCourse.getLongitude(), golfCourse.getLatitude(), List.of(32))
                .stream()
                .filter(item -> cat3Codes.contains(item.getCat3()))
                .map(this::toRecommendedPlace)
                .toList();

        // 각 타입별로 하나씩만 선택 (거리순 or 랜덤)
        RecommendedPlaceDto food = foodList.stream().findFirst().orElse(null);
        RecommendedPlaceDto tour = tourList.stream().findFirst().orElse(null);
        RecommendedPlaceDto stay = stayList.stream().findFirst().orElse(null);

        // 저장 (Entity로 변환 생략 가능)
        CourseRecommendation saved = courseRecommendationRepository.save(
                CourseRecommendation.create(golfCourse, courseType, teeOff, endTime, List.of(food, tour, stay))
        );

        return CourseRecommendationResponseDto.of(saved);
    }

    private List<String> getRecommendationOrder(LocalTime endTime) {
        if (endTime.isBefore(LocalTime.of(13, 0))) return List.of("food", "tour", "stay");
        if (endTime.isBefore(LocalTime.of(17, 0))) return List.of("tour", "food", "stay");
        return List.of("food", "stay"); // 관광 생략
    }

    private RecommendedPlaceDto toRecommendedPlace(TourItem item) {
        return RecommendedPlaceDto.builder()
                .type(resolveType(item.getContenttypeid())) // "food", "tour", "stay"
                .name(item.getTitle())
                .address(item.getAddr1())
                .imageUrl(item.getFirstimage())
                .distanceKm(0.0) // 필요 시 거리 계산 로직 추가
                .mapx(item.getMapx())
                .mapy(item.getMapy())
                .build();
    }

    public CourseRecommendationResponseDto getRecommendation(Long id) {
        CourseRecommendation course = courseRecommendationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("추천 코스를 찾을 수 없습니다."));

        return CourseRecommendationResponseDto.of(course);
    }

    public CourseRecommendationResponseDto updateRecommendation(Long id, CourseRecommendationRequestDto request) {

        CourseRecommendation course = courseRecommendationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("추천 코스를 찾을 수 없습니다."));

        // 기존 장소 삭제
        course.getRecommendedPlaces().clear();

        // 골프장 정보는 유지 / 시간 및 타입 재설정
        LocalTime teeOff = LocalTime.parse(request.getTeeOffTime());
        LocalTime endTime = teeOff.plusHours(4).plusMinutes(30);

        course.setTeeOffTime(teeOff);
        course.setEndTime(endTime);
        course.setCourseType(request.getCourseType());
        course.setCourseTypeLabel(resolveLabel(request.getCourseType()));
        course.setRecommendationOrder(getRecommendationOrder(endTime));

        // 새로운 장소로 다시 세팅
        List<RecommendedPlaceDto> foodList = tourApiClient.fetchNearbyItems(
                        course.getGolfCourse().getLongitude(), course.getGolfCourse().getLatitude(), List.of(39))
                .stream().map(this::toRecommendedPlace).toList();

        List<RecommendedPlaceDto> tourList = tourApiClient.fetchNearbyItems(
                        course.getGolfCourse().getLongitude(), course.getGolfCourse().getLatitude(), List.of(12))
                .stream().map(this::toRecommendedPlace).toList();

        List<String> cat3List = CourseTypeMapper.getCat3Codes(request.getCourseType());
        List<RecommendedPlaceDto> stayList = tourApiClient.fetchNearbyItems(
                        course.getGolfCourse().getLongitude(), course.getGolfCourse().getLatitude(), List.of(32))
                .stream().filter(item -> cat3List.contains(item.getCat3()))
                .map(this::toRecommendedPlace).toList();

        RecommendedPlaceDto food = foodList.stream().findFirst().orElse(null);
        RecommendedPlaceDto tour = tourList.stream().findFirst().orElse(null);
        RecommendedPlaceDto stay = stayList.stream().findFirst().orElse(null);

        if (food != null) course.getRecommendedPlaces().add(RecommendedPlace.of(food, course));
        if (tour != null) course.getRecommendedPlaces().add(RecommendedPlace.of(tour, course));
        if (stay != null) course.getRecommendedPlaces().add(RecommendedPlace.of(stay, course));

        // JPA는 Dirty Checking → save 생략 가능하지만 명시적 save도 OK
        courseRecommendationRepository.save(course);

        return CourseRecommendationResponseDto.of(course);
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
}

