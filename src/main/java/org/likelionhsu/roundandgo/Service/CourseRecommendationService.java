package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Common.ScheduleColor;
import org.likelionhsu.roundandgo.Dto.Request.ScheduleRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.CourseRecommendationResponseDto;
import org.likelionhsu.roundandgo.Dto.Api.RecommendedPlaceDto;
import org.likelionhsu.roundandgo.Dto.Api.TourItem;
import org.likelionhsu.roundandgo.Entity.*;
import org.likelionhsu.roundandgo.ExternalApi.TourApiClient;
import org.likelionhsu.roundandgo.ExternalApi.OpenAiApiClient;
import org.likelionhsu.roundandgo.Mapper.CourseTypeMapper;
import org.likelionhsu.roundandgo.Mapper.RegionCodeMapper;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseRecommendationService {

    private final GolfCourseRepository golfCourseRepository;
    private final CourseRecommendationRepository courseRecommendationRepository;
    private final TourApiClient tourApiClient;
    private final ScheduleService scheduleService;
    private final OpenAiApiClient openAiApiClient; // GPT API 클라이언트 추가
    private final RegionCodeMapper regionCodeMapper; // 지역 코드 매퍼 추가

    public CourseRecommendationResponseDto createRecommendation(User user, Long golfCourseId, String teeOffTime, String courseType) {

        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장 정보를 찾을 수 없습니다."));

        LocalTime teeOff = LocalTime.parse(teeOffTime);
        LocalTime endTime = teeOff.plusHours(4).plusMinutes(30);
        List<String> order = getRecommendationOrder(endTime);

        List<RecommendedPlaceDto> foodList = getPlaces(golfCourse, 39);
        List<RecommendedPlaceDto> tourList = getPlaces(golfCourse, 12);
        List<RecommendedPlaceDto> stayList = getFilteredStays(golfCourse, courseType);

        // null 체크를 통해 안전하게 리스트 생성
        List<RecommendedPlaceDto> recommendedPlaces = new ArrayList<>();

        RecommendedPlaceDto food = foodList.stream().findFirst().orElse(null);
        RecommendedPlaceDto tour = tourList.stream().findFirst().orElse(null);
        RecommendedPlaceDto stay = stayList.stream().findFirst().orElse(null);

        if (food != null) recommendedPlaces.add(food);
        if (tour != null) recommendedPlaces.add(tour);
        if (stay != null) recommendedPlaces.add(stay);

        CourseRecommendation saved = courseRecommendationRepository.save(
                CourseRecommendation.create(golfCourse, courseType, teeOff, endTime, recommendedPlaces, user)
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
        // 기존 추천순서(RecommendationOrder) 리스트 삭제 및 새로 추가
        course.getRecommendationOrders().clear();
        for (String orderType : getRecommendationOrder(endTime)) {
            RecommendationOrder order = new RecommendationOrder();
            order.setType(orderType);
            order.setCourseRecommendation(course);
            course.getRecommendationOrders().add(order);
        }

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

    // 2일 연속 골프장 방문을 위한 새로운 추천 생성 메서드
    public List<CourseRecommendationResponseDto> createMultiDayRecommendation(
            User user,
            List<Long> golfCourseIds,
            List<String> teeOffTimes,
            String courseType,
            LocalDate startDate,
            Integer travelDays) {

        if (golfCourseIds.size() != teeOffTimes.size()) {
            throw new IllegalArgumentException("골프장 수와 티오프 시간 수가 일치하지 않습니다.");
        }

        if (travelDays != golfCourseIds.size()) {
            throw new IllegalArgumentException("여행 기간과 골프장 수가 일치하지 않습니다.");
        }

        List<CourseRecommendationResponseDto> recommendations = new ArrayList<>();

        for (int i = 0; i < travelDays; i++) {
            Long golfCourseId = golfCourseIds.get(i);
            String teeOffTime = teeOffTimes.get(i);
            Integer dayNumber = i + 1;
            LocalDate currentDate = startDate.plusDays(i);

            GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                    .orElseThrow(() -> new RuntimeException("골프장 정보를 찾을 수 없습니다: " + golfCourseId));

            LocalTime teeOff = LocalTime.parse(teeOffTime);
            LocalTime endTime = teeOff.plusHours(4).plusMinutes(30);

            // 각 날짜별로 장소 추천
            List<RecommendedPlaceDto> foodList = getPlaces(golfCourse, 39);
            List<RecommendedPlaceDto> tourList = getPlaces(golfCourse, 12);
            List<RecommendedPlaceDto> stayList = getFilteredStays(golfCourse, courseType);

            // null 체크를 통해 안전하게 리스트 생성
            List<RecommendedPlaceDto> recommendedPlaces = new ArrayList<>();

            RecommendedPlaceDto food = foodList.stream().findFirst().orElse(null);
            RecommendedPlaceDto tour = tourList.stream().findFirst().orElse(null);
            RecommendedPlaceDto stay = stayList.stream().findFirst().orElse(null);

            if (food != null) recommendedPlaces.add(food);
            if (tour != null) recommendedPlaces.add(tour);
            if (stay != null) recommendedPlaces.add(stay);

            // 새로운 create 메서드 사용
            CourseRecommendation saved = courseRecommendationRepository.save(
                    CourseRecommendation.create(
                            golfCourse,
                            courseType,
                            teeOff,
                            endTime,
                            recommendedPlaces,
                            user,
                            startDate,
                            travelDays,
                            dayNumber
                    )
            );

            // 스케줄 생성
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            createSchedulesForRecommendation(user, golfCourse, dateStr, saved);

            recommendations.add(CourseRecommendationResponseDto.of(saved));
        }

        return recommendations;
    }

    // GPT를 활용한 AI 추천 메서드 추가
    public CourseRecommendationResponseDto createAiRecommendation(
            User user,
            Long golfCourseId,
            String teeOffTime,
            String courseType,
            String userPreferences) {

        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장 정보를 찾을 수 없습니다."));

        LocalTime teeOff = LocalTime.parse(teeOffTime);
        LocalTime endTime = teeOff.plusHours(4).plusMinutes(30);

        // 관광 데이터 API에서 장소 정보 가져오기
        List<RecommendedPlaceDto> foodList = getPlaces(golfCourse, 39);
        List<RecommendedPlaceDto> tourList = getPlaces(golfCourse, 12);
        List<RecommendedPlaceDto> stayList = getFilteredStays(golfCourse, courseType);

        // GPT에게 전달할 프롬프트 생성
        String prompt = buildPromptForGpt(golfCourse, courseType, teeOff, endTime,
                                        foodList, tourList, stayList, userPreferences);

        // GPT API 호출
        String gptRecommendation = openAiApiClient.generateCourseRecommendation(prompt);

        // GPT 응답을 파싱하여 최적의 장소 선택
        List<RecommendedPlaceDto> selectedPlaces = parseGptRecommendation(
                gptRecommendation, foodList, tourList, stayList);

        // 추천 코스 저장
        CourseRecommendation saved = courseRecommendationRepository.save(
                CourseRecommendation.create(golfCourse, courseType, teeOff, endTime, selectedPlaces, user)
        );

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        createSchedulesForRecommendation(user, golfCourse, date, saved);

        return CourseRecommendationResponseDto.of(saved);
    }

    // GPT를 활용한 2일 연속 AI 추천 메서드
    public List<CourseRecommendationResponseDto> createMultiDayAiRecommendation(
            User user,
            List<Long> golfCourseIds,
            List<String> teeOffTimes,
            String courseType,
            LocalDate startDate,
            Integer travelDays,
            String userPreferences) {

        if (golfCourseIds.size() != teeOffTimes.size()) {
            throw new IllegalArgumentException("골프장 수와 티오프 시간 수가 일치하지 않습니다.");
        }

        if (travelDays != golfCourseIds.size()) {
            throw new IllegalArgumentException("여행 기간과 골프장 수가 일치하지 않습니다.");
        }

        List<CourseRecommendationResponseDto> recommendations = new ArrayList<>();

        // 전체 여행 계획을 위한 종합 프롬프트 생성
        String multiDayPrompt = buildMultiDayPromptForGpt(
                golfCourseIds, teeOffTimes, courseType, startDate, travelDays, userPreferences);

        System.out.println("=== Multi-day AI Prompt ===");
        System.out.println(multiDayPrompt);
        System.out.println("===========================");

        // GPT에게 전체 여행 계획 요청
        String gptMultiDayRecommendation = openAiApiClient.generateCourseRecommendation(multiDayPrompt);

        System.out.println("=== GPT Response ===");
        System.out.println(gptMultiDayRecommendation);
        System.out.println("====================");

        for (int i = 0; i < travelDays; i++) {
            Long golfCourseId = golfCourseIds.get(i);
            String teeOffTime = teeOffTimes.get(i);
            Integer dayNumber = i + 1;
            LocalDate currentDate = startDate.plusDays(i);

            GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                    .orElseThrow(() -> new RuntimeException("골프장 정보를 찾을 수 없습니다: " + golfCourseId));

            LocalTime teeOff = LocalTime.parse(teeOffTime);
            LocalTime endTime = teeOff.plusHours(4).plusMinutes(30);

            // 각 날짜별로 장소 데이터 가져오기
            List<RecommendedPlaceDto> foodList = getPlaces(golfCourse, 39);
            List<RecommendedPlaceDto> tourList = getPlaces(golfCourse, 12);
            List<RecommendedPlaceDto> stayList = getFilteredStays(golfCourse, courseType);

            System.out.println("=== Day " + dayNumber + " Places Debug ===");
            System.out.println("Golf Course: " + golfCourse.getName());
            System.out.println("Food places count: " + foodList.size());
            System.out.println("Tour places count: " + tourList.size());
            System.out.println("Stay places count: " + stayList.size());

            if (!foodList.isEmpty()) {
                System.out.println("Sample food: " + foodList.get(0).getName());
            }
            if (!tourList.isEmpty()) {
                System.out.println("Sample tour: " + tourList.get(0).getName());
            }
            if (!stayList.isEmpty()) {
                System.out.println("Sample stay: " + stayList.get(0).getName());
            }
            System.out.println("===============================");

            // GPT 응답에서 해당 일차의 추천 파싱
            List<RecommendedPlaceDto> selectedPlaces = parseMultiDayGptRecommendation(
                    gptMultiDayRecommendation, dayNumber, foodList, tourList, stayList);

            System.out.println("=== Selected Places for Day " + dayNumber + " ===");
            System.out.println("Selected places count: " + selectedPlaces.size());
            selectedPlaces.forEach(place -> System.out.println("- " + place.getName() + " (" + place.getType() + ")"));
            System.out.println("=======================================");

            CourseRecommendation saved = courseRecommendationRepository.save(
                    CourseRecommendation.create(
                            golfCourse,
                            courseType,
                            teeOff,
                            endTime,
                            selectedPlaces,
                            user,
                            startDate,
                            travelDays,
                            dayNumber
                    )
            );

            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            createSchedulesForRecommendation(user, golfCourse, dateStr, saved);

            recommendations.add(CourseRecommendationResponseDto.of(saved));
        }

        return recommendations;
    }

    // GPT 프롬프트 생성 메서드
    private String buildPromptForGpt(
            GolfCourse golfCourse,
            String courseType,
            LocalTime teeOffTime,
            LocalTime endTime,
            List<RecommendedPlaceDto> foodList,
            List<RecommendedPlaceDto> tourList,
            List<RecommendedPlaceDto> stayList,
            String userPreferences) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("제주도 골프 여행 최적 일정 추천을 부탁드립니다.\n\n");
        prompt.append("**임무:** 골프 이후 시간을 활용한 최적의 여행 일정을 자유롭게 구성해주세요.\n\n");
        prompt.append("**여행 정보:**\n");
        prompt.append("- 골프장명: ").append(golfCourse.getName()).append("\n");
        prompt.append("- 주소: ").append(golfCourse.getAddress()).append("\n");
        prompt.append("- 골프 시간: ").append(teeOffTime).append(" ~ ").append(endTime).append("\n");
        prompt.append("- 코스 타입: ").append(resolveLabel(courseType)).append("\n\n");

        // 시간 제약 조건
        LocalTime availableStartTime = endTime.plusMinutes(30); // 골프 종료 30분 후부터 가능

        prompt.append("**시간 제약 조건:**\n");
        prompt.append("- 골프 종료: ").append(endTime).append("\n");
        prompt.append("- 추가 활동 가능 시간: ").append(availableStartTime).append(" 이후\n");
        prompt.append("- 골프 시간과 절대 겹치지 않도록 해주세요!\n\n");

        if (userPreferences != null && !userPreferences.trim().isEmpty()) {
            prompt.append("**사용자 선호사항:**\n");
            prompt.append(userPreferences).append("\n\n");
        }

        prompt.append("**이용 가능한 장소 옵션:**\n\n");

        prompt.append("**음식점 옵션 (필요시 선택):**\n");
        foodList.stream().limit(15).forEach(food ->
            prompt.append("- ").append(food.getName()).append(" (").append(food.getAddress()).append(")\n"));

        prompt.append("\n**관광지 옵션 (필요시 선택):**\n");
        tourList.stream().limit(15).forEach(tour ->
            prompt.append("- ").append(tour.getName()).append(" (").append(tour.getAddress()).append(")\n"));

        if (!stayList.isEmpty()) {
            prompt.append("\n**숙소 옵션 (숙박 필요시 선택):**\n");
            stayList.stream().limit(10).forEach(stay ->
                prompt.append("- ").append(stay.getName()).append(" (").append(stay.getAddress()).append(")\n"));
        }

        prompt.append("\n**추천 가이드라인:**\n");
        prompt.append("1. **자유로운 일정 구성**: 꼭 음식점+관광지+숙소 조합이 아니어도 됩니다\n");
        prompt.append("2. **실용적 추천**: \n");
        prompt.append("   - 점심 시간대(12:00-15:00)라면 식사 장소 추천\n");
        prompt.append("   - 저녁 시간대(17:00-20:00)라면 저녁 식사 고려\n");
        prompt.append("   - 늦은 시간이면 간단한 카페나 휴식 공간도 좋음\n");
        prompt.append("   - 당일 여행이면 숙소 불필요\n");
        prompt.append("   - 1박2일이면 숙소 필요\n");
        prompt.append("3. **이동 거리 최소화**: 골프장 근처 장소 우선 고려\n");
        prompt.append("4. **시간 효율성**: 각 활동 간 30분 이동 시간 확보\n");
        prompt.append("5. **다양한 패턴 가능**:\n");
        prompt.append("   - 점심만 (골프 후 점심 후 귀가)\n");
        prompt.append("   - 점심 + 관광 (오후 여행)\n");
        prompt.append("   - 점심 + 관광 + 저녁\n");
        prompt.append("   - 관광 + 저녁 (점심 생략)\n");
        prompt.append("   - 카페 + 쇼핑 등 자유로운 조합\n\n");

        prompt.append("**응답 형식:**\n");
        prompt.append("추천하고 싶은 장소들을 | 구분자로 나열해주세요.\n");
        prompt.append("장소명은 반드시 위에 제공된 목록에서 선택해주세요.\n\n");
        prompt.append("예시:\n");
        prompt.append("- 점심만: 제주흑돼지맛집\n");
        prompt.append("- 점심+관광: 제주흑돼지맛집|성산일출봉\n");
        prompt.append("- 점심+관광+숙소: 제주흑돼지맛집|성산일출봉|제주신라호텔\n");
        prompt.append("- 관광+저녁: 성산일출봉|해산물뚝배기\n\n");

        // 다양성 참고 정보
        long currentTime = System.currentTimeMillis();
        prompt.append("**참고정보:** 추천 요청 시각: ").append(currentTime % 10000).append("\n");
        prompt.append("골프 종료 시간과 현재 상황을 고려하여 가장 적절한 일정을 자유롭게 추천해주세요.");

        return prompt.toString();
    }

    // 다일차 여행 GPT 프롬프트 생성 메서드
    private String buildMultiDayPromptForGpt(
            List<Long> golfCourseIds,
            List<String> teeOffTimes,
            String courseType,
            LocalDate startDate,
            Integer travelDays,
            String userPreferences) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("제주도 ").append(travelDays).append("일 골프 여행 최적 코스 추천을 부탁드립니다.\n\n");
        prompt.append("**임무:** 이동 거리를 최소화하고 시간대 겹침을 방지한 최적의 ").append(travelDays).append("일 골프 여행 코스를 추천하는 것이 목표입니다.\n\n");
        prompt.append("**여행 기간:** ").append(startDate).append("부터 ").append(travelDays).append("일간\n");
        prompt.append("**코스 타입:** ").append(resolveLabel(courseType)).append("\n\n");

        if (userPreferences != null && !userPreferences.trim().isEmpty()) {
            prompt.append("**사용자 선호사항:**\n");
            prompt.append(userPreferences).append("\n\n");
        }

        // 각 일차별 상세 시간 계획 추가
        for (int i = 0; i < travelDays; i++) {
            LocalTime teeOffTime = LocalTime.parse(teeOffTimes.get(i));
            LocalTime golfEndTime = teeOffTime.plusHours(4).plusMinutes(30);
            LocalTime lunchTime = golfEndTime.plusMinutes(30);
            LocalTime tourTime = lunchTime.plusHours(1).plusMinutes(30);

            prompt.append("**").append(i + 1).append("일차 시간 계획:**\n");
            prompt.append("- 골프장 ID: ").append(golfCourseIds.get(i)).append("\n");
            prompt.append("- 골프 시간: ").append(teeOffTime).append(" ~ ").append(golfEndTime).append("\n");
            prompt.append("- 권장 점심 시간: ").append(lunchTime).append(" 이후 (골프 종료 후 이동시간 고려)\n");
            prompt.append("- 권장 관광 시간: ").append(tourTime).append(" 이후 (점심 후 이동시간 고려)\n");
            prompt.append("- **중요:** 골프 시간과 다른 활동 시간이 절대 겹치면 안됩니다!\n\n");
        }

        prompt.append("**중요한 요청사항:**\n");
        prompt.append("1. 각 일차별로 서로 다른 음식점과 관광지를 추천해주세요 (중복 금지)\n");
        prompt.append("2. 숙소는 같아도 되지만, 음식점과 관광지는 반드시 다른 곳으로 선택해주세요\n");
        prompt.append("3. 골프장과의 이동 거리를 최소화하여 효율적인 동선을 고려해주세요\n");
        prompt.append("4. 각 활동 간 이동 시간을 최소 30분씩 확보해주세요\n");
        prompt.append("5. 골프 종료 시간 이후에만 다른 활동이 가능합니다\n");
        prompt.append("6. 일차별로 음식점, 관광지, 숙소를 각각 1곳씩 추천해주세요\n");
        prompt.append("7. 최적 동선: 골프장 → 음식점 → 관광지 → 숙소 순서로 제안해주세요\n");
        prompt.append("8. 일차별로 다양성을 고려한 최적의 여행 코스를 추천해주세요\n\n");

        // 현재 시간을 기반으로 다양성 추가
        long currentTime = System.currentTimeMillis();
        prompt.append("**참고정보:** 추천 요청 시각: ").append(currentTime % 10000).append("\n");
        prompt.append("위 시각 정보를 참고하여 더욱 다양한 추천을 제공해주세요.\n\n");

        prompt.append("응답 형식: [1일차] 음식점명|관광지명|숙소명 [2일차] 음식점명|관광지명|숙소명\n");
        prompt.append("예시: [1일차] 제주흑돼지맛집|성산일출봉|제주신라호텔 [2일차] 해산물뚝배기|한라산국립공원|롯데호텔제주");

        return prompt.toString();
    }

    // GPT 응답 파싱 메서드 (단일일) - 자유로운 일정 구성 지원
    private List<RecommendedPlaceDto> parseGptRecommendation(
            String gptResponse,
            List<RecommendedPlaceDto> foodList,
            List<RecommendedPlaceDto> tourList,
            List<RecommendedPlaceDto> stayList) {

        List<RecommendedPlaceDto> selectedPlaces = new ArrayList<>();

        try {
            System.out.println("=== GPT Response Parsing (Flexible) ===");
            System.out.println("GPT Response: " + gptResponse);

            // GPT 응답을 | 구분자로 분리
            String[] recommendedPlaceNames = gptResponse.split("\\|");

            System.out.println("Split places count: " + recommendedPlaceNames.length);

            // 각 장소명에 대해 전체 카테고리에서 매칭 시도
            for (String placeName : recommendedPlaceNames) {
                placeName = placeName.trim();

                // 태그 제거 ([음식점], [관광지], [숙소] 등)
                placeName = placeName.replaceAll("\\[.*?\\]", "").trim();

                if (placeName.isEmpty()) continue;

                System.out.println("Trying to match: " + placeName);

                RecommendedPlaceDto matchedPlace = null;

                // 1. 음식점에서 찾기
                matchedPlace = findExactMatch(placeName, foodList);
                if (matchedPlace != null) {
                    System.out.println("Found in food: " + matchedPlace.getName());
                    selectedPlaces.add(matchedPlace);
                    continue;
                }

                // 2. 관광지에서 찾기
                matchedPlace = findExactMatch(placeName, tourList);
                if (matchedPlace != null) {
                    System.out.println("Found in tour: " + matchedPlace.getName());
                    selectedPlaces.add(matchedPlace);
                    continue;
                }

                // 3. 숙소에서 찾기
                matchedPlace = findExactMatch(placeName, stayList);
                if (matchedPlace != null) {
                    System.out.println("Found in stay: " + matchedPlace.getName());
                    selectedPlaces.add(matchedPlace);
                    continue;
                }

                System.out.println("No exact match found for: " + placeName);
            }

            System.out.println("Final selected places count: " + selectedPlaces.size());

        } catch (Exception e) {
            System.out.println("GPT parsing error: " + e.getMessage());

            // 파싱 실패시 기본 추천 - 시간대에 따라 적절한 장소 선택
            LocalTime currentTime = LocalTime.now();

            if (currentTime.isBefore(LocalTime.of(15, 0))) {
                // 오후 3시 이전이면 점심 추천
                RecommendedPlaceDto food = foodList.stream().findFirst().orElse(null);
                if (food != null) selectedPlaces.add(food);
            }

            if (currentTime.isBefore(LocalTime.of(18, 0))) {
                // 오후 6시 이전이면 관광지 추천
                RecommendedPlaceDto tour = tourList.stream().findFirst().orElse(null);
                if (tour != null) selectedPlaces.add(tour);
            }
        }

        return selectedPlaces;
    }

    // 정확한 매칭을 위한 헬퍼 메서드
    private RecommendedPlaceDto findExactMatch(String targetName, List<RecommendedPlaceDto> places) {
        // 1. 정확한 이름 매칭
        for (RecommendedPlaceDto place : places) {
            if (place.getName().equals(targetName)) {
                return place;
            }
        }

        // 2. 부분 매칭 (양방향)
        for (RecommendedPlaceDto place : places) {
            if (place.getName().contains(targetName) || targetName.contains(place.getName())) {
                return place;
            }
        }

        // 3. 키워드 매칭
        String[] keywords = targetName.split("[\\s\\-]+");
        for (String keyword : keywords) {
            keyword = keyword.trim();
            if (keyword.length() > 1) {
                for (RecommendedPlaceDto place : places) {
                    if (place.getName().contains(keyword)) {
                        return place;
                    }
                }
            }
        }

        return null;
    }


    public List<RecommendedPlaceDto> getPlaces(GolfCourse golfCourse, int contentTypeId) {
        // 골프장 좌표 정보 추출
        double mapX = golfCourse.getLongitude(); // 경도 (X 좌표)
        double mapY = golfCourse.getLatitude();  // 위도 (Y 좌표)

        System.out.println("=== getPlaces Debug (Coordinate-based) ===");
        System.out.println("Golf Course: " + golfCourse.getName());
        System.out.println("Address: " + golfCourse.getAddress());
        System.out.println("Coordinates: longitude=" + mapX + ", latitude=" + mapY);
        System.out.println("ContentTypeId: " + contentTypeId);

        // 좌표 기반으로 주변 관광지 검색 (최대 20km 반경)
        List<TourItem> tourItems = tourApiClient.fetchNearbyItems(mapX, mapY, List.of(contentTypeId));

        System.out.println("Retrieved nearby items count: " + tourItems.size());
        if (!tourItems.isEmpty()) {
            System.out.println("First 3 items:");
            tourItems.stream().limit(3).forEach(item ->
                System.out.println("- " + item.getTitle() + " (거리: " + item.getDist() + "m)"));
        }
        System.out.println("==========================================");

        return tourItems.stream()
                .map(this::toRecommendedPlace)
                .toList();
    }

    private List<RecommendedPlaceDto> getFilteredStays(GolfCourse golfCourse, String courseType) {
        // 골프장 좌표 정보 추출
        double mapX = golfCourse.getLongitude(); // 경도 (X 좌표)
        double mapY = golfCourse.getLatitude();  // 위도 (Y 좌표)

        System.out.println("=== getFilteredStays Debug (Coordinate-based) ===");
        System.out.println("Golf Course: " + golfCourse.getName());
        System.out.println("Address: " + golfCourse.getAddress());
        System.out.println("Coordinates: longitude=" + mapX + ", latitude=" + mapY);
        System.out.println("Course Type: " + courseType);

        // 좌표 기반으로 주변 숙소 검색 (최대 20km 반경)
        List<TourItem> tourItems = tourApiClient.fetchNearbyItems(mapX, mapY, List.of(32));

        System.out.println("Retrieved nearby stay items count: " + tourItems.size());

        // 코스 타입에 따른 필터링
        List<String> cat3Codes = CourseTypeMapper.getCat3Codes(courseType);
        List<TourItem> filteredItems = tourItems.stream()
                .filter(item -> cat3Codes.contains(item.getCat3()))
                .toList();

        System.out.println("Filtered stay items count: " + filteredItems.size());
        if (!filteredItems.isEmpty()) {
            System.out.println("First 3 filtered stays:");
            filteredItems.stream().limit(3).forEach(item ->
                System.out.println("- " + item.getTitle() + " (거리: " + item.getDist() + "m)"));
        }
        System.out.println("===============================================");

        return filteredItems.stream()
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
                // 시간 정보는 응답 생성 시점에서 계산되므로 여기서는 null로 설정
                .startTime(null)
                .endTime(null)
                .duration(null)
                .timeLabel(null)
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
        LocalTime teeOffTime = courseRecommendation.getTeeOffTime();
        LocalTime golfEndTime = courseRecommendation.getEndTime();

        // (1) 골프장 일정 - 실제 시간대로 설정
        ScheduleRequestDto golfSchedule = new ScheduleRequestDto();
        golfSchedule.setTitle("[라운딩] " + golfCourse.getName());
        golfSchedule.setAllDay(false); // 시간 지정으로 변경
        golfSchedule.setCategory("라운딩");
        golfSchedule.setLocation(golfCourse.getAddress());
        golfSchedule.setColor(ScheduleColor.GREEN);
        String golfStartDateTime = date + "T" + teeOffTime.toString() + ":00";
        String golfEndDateTime = date + "T" + golfEndTime.toString() + ":00";
        scheduleService.createSchedule(user, golfSchedule, golfStartDateTime, golfEndDateTime);

        // (2) 추천 장소(음식점, 관광지, 숙소) 일정 - 시간 간격을 두고 생성
        List<RecommendedPlace> recommendedPlaces = courseRecommendation.getRecommendedPlaces();

        LocalTime currentTime = golfEndTime.plusMinutes(30); // 골프 종료 30분 후부터 시작

        for (RecommendedPlace place : recommendedPlaces) {
            ScheduleRequestDto placeSchedule = new ScheduleRequestDto();
            placeSchedule.setTitle("[추천] " + place.getName());
            placeSchedule.setAllDay(false); // 시간 지정으로 변경
            placeSchedule.setCategory(place.getType());
            placeSchedule.setLocation(place.getAddress());
            placeSchedule.setColor(ScheduleColor.BLUE);

            // 장소 유형에 따라 적절한 시간 배정
            LocalTime endTime;
            switch (place.getType()) {
                case "food" -> {
                    endTime = currentTime.plusHours(1).plusMinutes(30); // 음식점 1시간 30분
                }
                case "tour" -> {
                    endTime = currentTime.plusHours(2); // 관광지 2시간
                }
                case "stay" -> {
                    endTime = LocalTime.of(23, 59); // 숙소는 저녁까지
                }
                default -> {
                    endTime = currentTime.plusHours(1); // 기본 1시간
                }
            }

            String placeStartDateTime = date + "T" + currentTime.toString() + ":00";
            String placeEndDateTime = date + "T" + endTime.toString() + ":00";

            scheduleService.createSchedule(user, placeSchedule, placeStartDateTime, placeEndDateTime);

            // 다음 장소를 위한 시간 업데이트 (이동 시간 30분 추가)
            if (!place.getType().equals("stay")) {
                currentTime = endTime.plusMinutes(30);
            }
        }
    }

    // 다일차 응답에서 특정 일차 정보 추출
    private String extractDayRecommendation(String gptResponse, String dayPattern) {
        try {
            int startIndex = gptResponse.indexOf(dayPattern);
            if (startIndex == -1) return null;

            // "]" 다음부터 실제 추천 내용 찾기
            int bracketEnd = gptResponse.indexOf("]", startIndex);
            if (bracketEnd == -1) return null;

            // "]" 이후부터 다음 줄바꿈이나 특정 패턴까지만 추출
            String afterBracket = gptResponse.substring(bracketEnd + 1);

            // 첫 번째 줄만 추출 (상세 설명 제외)
            String[] lines = afterBracket.split("\\n");
            String firstLine = lines[0].trim();

            // "### 상세 설명" 같은 패턴이 있으면 그 전까지만 추출
            int detailIndex = firstLine.indexOf("###");
            if (detailIndex != -1) {
                firstLine = firstLine.substring(0, detailIndex).trim();
            }

            // "**1일차:**" 같은 패턴이 있으면 그 전까지만 추출
            int dayDetailIndex = firstLine.indexOf("**" + dayPattern.replace("일차", "") + "일차:**");
            if (dayDetailIndex != -1) {
                firstLine = firstLine.substring(0, dayDetailIndex).trim();
            }

            System.out.println("=== extractDayRecommendation Debug ===");
            System.out.println("dayPattern: " + dayPattern);
            System.out.println("Original content length: " + afterBracket.length());
            System.out.println("Extracted first line: " + firstLine);
            System.out.println("=====================================");

            return firstLine;
        } catch (Exception e) {
            System.out.println("extractDayRecommendation error: " + e.getMessage());
            return null;
        }
    }

    // 다일차 GPT 응답 파싱 메서드 (다일차)
    private List<RecommendedPlaceDto> parseMultiDayGptRecommendation(
            String gptResponse,
            Integer dayNumber,
            List<RecommendedPlaceDto> foodList,
            List<RecommendedPlaceDto> tourList,
            List<RecommendedPlaceDto> stayList) {

        List<RecommendedPlaceDto> selectedPlaces = new ArrayList<>();

        // GPT 응답이 오류 메시지인 경우 다양성을 위한 랜덤 선택 사용
        if (gptResponse.contains("오류가 발생했습니다") || gptResponse.contains("생성할 수 없습니다")) {
            System.out.println("=== GPT Error - Using Diversified Default Recommendation ===");
            selectedPlaces.addAll(selectDiversifiedPlaces(dayNumber, foodList, tourList, stayList));
            return selectedPlaces;
        }

        try {
            // GPT 응답에서 해당 일차의 추천 정보 추출
            String dayPattern = dayNumber + "일차";
            String dayRecommendation = extractDayRecommendation(gptResponse, dayPattern);

            if (dayRecommendation != null && !dayRecommendation.trim().isEmpty()) {
                // 자유로운 파싱 로직 적용
                String[] recommendedPlaceNames = dayRecommendation.split("\\|");

                // 각 장소명에 대해 전체 카테고리에서 매칭 시도
                for (String placeName : recommendedPlaceNames) {
                    placeName = placeName.trim();
                    placeName = placeName.replaceAll("\\[.*?\\]", "").trim();

                    if (placeName.isEmpty()) continue;

                    RecommendedPlaceDto matchedPlace = null;

                    // 전체 카테고리에서 순차적으로 찾기
                    matchedPlace = findExactMatch(placeName, foodList);
                    if (matchedPlace != null) {
                        selectedPlaces.add(matchedPlace);
                        continue;
                    }

                    matchedPlace = findExactMatch(placeName, tourList);
                    if (matchedPlace != null) {
                        selectedPlaces.add(matchedPlace);
                        continue;
                    }

                    matchedPlace = findExactMatch(placeName, stayList);
                    if (matchedPlace != null) {
                        selectedPlaces.add(matchedPlace);
                        continue;
                    }
                }

                // 최소한의 추천이 없다면 기본값으로 채우기
                if (selectedPlaces.isEmpty()) {
                    System.out.println("=== No matches found - Using diversified selection ===");
                    selectedPlaces.addAll(selectDiversifiedPlaces(dayNumber, foodList, tourList, stayList));
                }
            } else {
                System.out.println("=== GPT Day Pattern Not Found - Using Diversified Selection ===");
                selectedPlaces.addAll(selectDiversifiedPlaces(dayNumber, foodList, tourList, stayList));
            }

        } catch (Exception e) {
            System.out.println("=== GPT Parsing Error - Using Diversified Default Recommendation ===");
            selectedPlaces.addAll(selectDiversifiedPlaces(dayNumber, foodList, tourList, stayList));
        }

        return selectedPlaces;
    }

    // 다양성을 위한 기본 추천 장소 선택 메서드
    private List<RecommendedPlaceDto> selectDiversifiedPlaces(Integer dayNumber, List<RecommendedPlaceDto> foodList, List<RecommendedPlaceDto> tourList, List<RecommendedPlaceDto> stayList) {
        List<RecommendedPlaceDto> diversifiedPlaces = new ArrayList<>();

        // 각 카테고리에서 하나씩 랜덤 선택
        RecommendedPlaceDto randomFood = foodList.stream().findAny().orElse(null);
        RecommendedPlaceDto randomTour = tourList.stream().findAny().orElse(null);
        RecommendedPlaceDto randomStay = stayList.stream().findAny().orElse(null);

        if (randomFood != null) diversifiedPlaces.add(randomFood);
        if (randomTour != null) diversifiedPlaces.add(randomTour);
        if (randomStay != null) diversifiedPlaces.add(randomStay);

        return diversifiedPlaces;
    }
}
