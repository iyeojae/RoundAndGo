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
        prompt.append("제주도 골프 여행 코스 추천을 부탁드립니다.\n\n");
        prompt.append("**골프장 정보:**\n");
        prompt.append("- 골프장명: ").append(golfCourse.getName()).append("\n");
        prompt.append("- 주소: ").append(golfCourse.getAddress()).append("\n");
        prompt.append("- 티오프 시간: ").append(teeOffTime).append("\n");
        prompt.append("- 예상 종료 시간: ").append(endTime).append("\n");
        prompt.append("- 코스 타입: ").append(resolveLabel(courseType)).append("\n\n");

        if (userPreferences != null && !userPreferences.trim().isEmpty()) {
            prompt.append("**사용자 선호사항:**\n");
            prompt.append(userPreferences).append("\n\n");
        }

        prompt.append("**이용 가능한 음식점 목록:**\n");
        foodList.stream().limit(10).forEach(food ->
            prompt.append("- ").append(food.getName()).append(" (").append(food.getAddress()).append(")\n"));

        prompt.append("\n**이용 가능한 관광지 목록:**\n");
        tourList.stream().limit(10).forEach(tour ->
            prompt.append("- ").append(tour.getName()).append(" (").append(tour.getAddress()).append(")\n"));

        prompt.append("\n**이용 가능한 숙소 목록:**\n");
        stayList.stream().limit(10).forEach(stay ->
            prompt.append("- ").append(stay.getName()).append(" (").append(stay.getAddress()).append(")\n"));

        prompt.append("\n**요청사항:**\n");
        prompt.append("위 정보를 바탕으로 최적의 음식점 1곳, 관광지 1곳, 숙소 1곳을 추천해주세요.\n");
        prompt.append("추천 이유와 함께 정확한 장소명을 제시해주세요.\n");
        prompt.append("응답 형식: [음식점] 장소명 | [관광지] 장소명 | [숙소] 장소명");

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
        prompt.append("제주도 ").append(travelDays).append("일 골프 여행 코스 추천을 부탁드립니다.\n\n");
        prompt.append("**여행 기간:** ").append(startDate).append("부터 ").append(travelDays).append("일간\n");
        prompt.append("**코스 타입:** ").append(resolveLabel(courseType)).append("\n\n");

        if (userPreferences != null && !userPreferences.trim().isEmpty()) {
            prompt.append("**사용자 선호사항:**\n");
            prompt.append(userPreferences).append("\n\n");
        }

        for (int i = 0; i < travelDays; i++) {
            prompt.append("**").append(i + 1).append("일차 계획:**\n");
            prompt.append("- 골프장 ID: ").append(golfCourseIds.get(i)).append("\n");
            prompt.append("- 티오프 시간: ").append(teeOffTimes.get(i)).append("\n\n");
        }

        prompt.append("**요청사항:**\n");
        prompt.append("각 일차별로 연계성을 고려한 최적의 여행 코스를 추천해주세요.\n");
        prompt.append("일차별로 음식점, 관광지, 숙소를 각각 1곳씩 추천해주세요.\n");
        prompt.append("응답 형식: [1일차] 음식점명|관광지명|숙소명 [2일차] 음식점명|관광지명|숙소명");

        return prompt.toString();
    }

    // GPT 응답 파싱 메서드 (단일일)
    private List<RecommendedPlaceDto> parseGptRecommendation(
            String gptResponse,
            List<RecommendedPlaceDto> foodList,
            List<RecommendedPlaceDto> tourList,
            List<RecommendedPlaceDto> stayList) {

        List<RecommendedPlaceDto> selectedPlaces = new ArrayList<>();

        try {
            // GPT 응답에서 장소명 추출하여 매칭
            RecommendedPlaceDto selectedFood = findBestMatch(gptResponse, foodList, "음식점");
            RecommendedPlaceDto selectedTour = findBestMatch(gptResponse, tourList, "관광지");
            RecommendedPlaceDto selectedStay = findBestMatch(gptResponse, stayList, "숙소");

            if (selectedFood != null) selectedPlaces.add(selectedFood);
            if (selectedTour != null) selectedPlaces.add(selectedTour);
            if (selectedStay != null) selectedPlaces.add(selectedStay);

        } catch (Exception e) {
            // GPT 파싱 실패시 기본 추천 방식 사용
            selectedPlaces.add(foodList.stream().findFirst().orElse(null));
            selectedPlaces.add(tourList.stream().findFirst().orElse(null));
            selectedPlaces.add(stayList.stream().findFirst().orElse(null));
        }

        return selectedPlaces.stream().filter(place -> place != null).toList();
    }

    // GPT 응답 파싱 메서드 (다일차)
    private List<RecommendedPlaceDto> parseMultiDayGptRecommendation(
            String gptResponse,
            Integer dayNumber,
            List<RecommendedPlaceDto> foodList,
            List<RecommendedPlaceDto> tourList,
            List<RecommendedPlaceDto> stayList) {

        List<RecommendedPlaceDto> selectedPlaces = new ArrayList<>();

        // GPT 응답이 오류 메시지인 경우 바로 기본 추천 사용
        if (gptResponse.contains("오류가 발생했습니다") || gptResponse.contains("생성할 수 없습니다")) {
            System.out.println("=== GPT Error - Using Default Recommendation ===");
            RecommendedPlaceDto defaultFood = foodList.stream().findFirst().orElse(null);
            RecommendedPlaceDto defaultTour = tourList.stream().findFirst().orElse(null);
            RecommendedPlaceDto defaultStay = stayList.stream().findFirst().orElse(null);

            if (defaultFood != null) selectedPlaces.add(defaultFood);
            if (defaultTour != null) selectedPlaces.add(defaultTour);
            if (defaultStay != null) selectedPlaces.add(defaultStay);

            return selectedPlaces.stream().filter(place -> place != null).toList();
        }

        try {
            // GPT 응답에서 해당 일차의 추천 정보 추출
            String dayPattern = dayNumber + "일차";
            String dayRecommendation = extractDayRecommendation(gptResponse, dayPattern);

            if (dayRecommendation != null) {
                RecommendedPlaceDto selectedFood = findBestMatch(dayRecommendation, foodList, "음식점");
                RecommendedPlaceDto selectedTour = findBestMatch(dayRecommendation, tourList, "관광지");
                RecommendedPlaceDto selectedStay = findBestMatch(dayRecommendation, stayList, "숙소");

                if (selectedFood != null) selectedPlaces.add(selectedFood);
                if (selectedTour != null) selectedPlaces.add(selectedTour);
                if (selectedStay != null) selectedPlaces.add(selectedStay);
            }

        } catch (Exception e) {
            System.out.println("=== GPT Parsing Error - Using Default Recommendation ===");
            // 파싱 실패시 기본 추천 방식 사용
            RecommendedPlaceDto defaultFood = foodList.stream().findFirst().orElse(null);
            RecommendedPlaceDto defaultTour = tourList.stream().findFirst().orElse(null);
            RecommendedPlaceDto defaultStay = stayList.stream().findFirst().orElse(null);

            if (defaultFood != null) selectedPlaces.add(defaultFood);
            if (defaultTour != null) selectedPlaces.add(defaultTour);
            if (defaultStay != null) selectedPlaces.add(defaultStay);
        }

        return selectedPlaces.stream().filter(place -> place != null).toList();
    }

    // GPT 응답에서 최적 매칭 장소 찾기
    private RecommendedPlaceDto findBestMatch(String gptResponse, List<RecommendedPlaceDto> places, String type) {
        return places.stream()
                .filter(place -> gptResponse.contains(place.getName()))
                .findFirst()
                .orElse(places.stream().findFirst().orElse(null));
    }

    // 다일차 응답에서 특정 일차 정보 추출
    private String extractDayRecommendation(String gptResponse, String dayPattern) {
        try {
            int startIndex = gptResponse.indexOf(dayPattern);
            if (startIndex == -1) return null;

            int endIndex = gptResponse.indexOf("]", startIndex);
            if (endIndex == -1) endIndex = gptResponse.length();

            return gptResponse.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }

    public List<RecommendedPlaceDto> getPlaces(GolfCourse golfCourse, int contentTypeId) {
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

