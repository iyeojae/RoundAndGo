package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Dto.Request.CourseRecommendationRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.CourseRecommendationResponseDto;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Service.CourseRecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/recommendation")
public class CourseRecommendationController {

    private final CourseRecommendationService recommendationService;

    // 코스 추천 생성 (사용자 정보 포함)
    @PostMapping
    public ResponseEntity<CommonResponse<CourseRecommendationResponseDto>> createCourse(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Long golfCourseId,
            @RequestParam String teeOffTime,
            @RequestParam String courseType
    ) {
        CourseRecommendationResponseDto dto = recommendationService.createRecommendation(userDetails.getUser(),
                golfCourseId, teeOffTime, courseType);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                CommonResponse.<CourseRecommendationResponseDto>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .msg("코스 추천 생성 성공")
                        .data(dto)
                        .build()
        );
    }

    // 마이페이지 - 내가 추천한 코스 목록 조회
    @GetMapping("/my")
    public ResponseEntity<CommonResponse<List<CourseRecommendationResponseDto>>> getMyRecommendations(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<CourseRecommendationResponseDto> list = recommendationService.getRecommendationsByUser(userDetails.getUser());

        return ResponseEntity.ok(
                CommonResponse.<List<CourseRecommendationResponseDto>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("나의 추천 코스 조회 성공")
                        .data(list)
                        .build()
        );
    }


    // ✅ 2. 단건 추천 코스 조회
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<CourseRecommendationResponseDto>> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(
                CommonResponse.<CourseRecommendationResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("추천 코스 단건 조회 성공")
                        .data(recommendationService.getRecommendation(id))
                        .build()
        );
    }

    // ✅ 3. 추천 코스 수정
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<CourseRecommendationResponseDto>> updateCourse(
            @PathVariable Long id,
            @RequestParam Long golfCourseId,
            @RequestParam String teeOffTime,
            @RequestParam String courseType,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        CourseRecommendationResponseDto dto = recommendationService.updateRecommendation(id, golfCourseId, teeOffTime, courseType, userDetails.getUser());
        return ResponseEntity.ok(
                CommonResponse.<CourseRecommendationResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("추천 코스 수정 성공")
                        .data(dto)
                        .build()
        );
    }

    // ✅ 4. 2일 연속 골프장 방문을 위한 코스 추천 생성
    @PostMapping("/multi-day")
    public ResponseEntity<CommonResponse<List<CourseRecommendationResponseDto>>> createMultiDayCourse(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CourseRecommendationRequestDto request
    ) {
        // 디버깅용 로그 추가
        System.out.println("=== Multi-day Course Request Debug ===");
        System.out.println("Request: " + request);
        System.out.println("GolfCourseIds: " + request.getGolfCourseIds());
        System.out.println("TeeOffTimes: " + request.getTeeOffTimes());
        System.out.println("StartDate: " + request.getStartDate());
        System.out.println("TravelDays: " + request.getTravelDays());
        System.out.println("===================================");

        // 요청 데이터 검증
        if (request.getGolfCourseIds() == null || request.getGolfCourseIds().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CommonResponse.<List<CourseRecommendationResponseDto>>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .msg("골프장 정보가 필요합니다.")
                            .build()
            );
        }

        if (request.getTeeOffTimes() == null || request.getTeeOffTimes().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CommonResponse.<List<CourseRecommendationResponseDto>>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .msg("티오프 시간 정보가 필요합니다.")
                            .build()
            );
        }

        if (request.getStartDate() == null || request.getTravelDays() == null) {
            return ResponseEntity.badRequest().body(
                    CommonResponse.<List<CourseRecommendationResponseDto>>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .msg("여행 시작 날짜와 기간 정보가 필요합니다.")
                            .build()
            );
        }

        List<CourseRecommendationResponseDto> recommendations = recommendationService.createMultiDayRecommendation(
                userDetails.getUser(),
                request.getGolfCourseIds(),
                request.getTeeOffTimes(),
                request.getCourseType(),
                request.getStartDate(),
                request.getTravelDays()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                CommonResponse.<List<CourseRecommendationResponseDto>>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .msg(request.getTravelDays() + "일 골프 여행 코스 추천 생성 성공")
                        .data(recommendations)
                        .build()
        );
    }

    // ✅ 5. GPT를 활용한 AI 코스 추천 생성
    @PostMapping("/ai")
    public ResponseEntity<CommonResponse<CourseRecommendationResponseDto>> createAiCourse(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Long golfCourseId,
            @RequestParam String teeOffTime,
            @RequestParam String courseType,
            @RequestParam(required = false) String userPreferences
    ) {
        CourseRecommendationResponseDto dto = recommendationService.createAiRecommendation(
                userDetails.getUser(), golfCourseId, teeOffTime, courseType, userPreferences);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                CommonResponse.<CourseRecommendationResponseDto>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .msg("AI 골프 여행 코스 추천 생성 성공")
                        .data(dto)
                        .build()
        );
    }

    // ✅ 6. GPT를 활용한 2일 연속 AI 코스 추천 생성
    @PostMapping("/ai/multi-day")
    public ResponseEntity<CommonResponse<List<CourseRecommendationResponseDto>>> createMultiDayAiCourse(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CourseRecommendationRequestDto request,
            @RequestParam(required = false) String userPreferences
    ) {
        // 요청 데이터 검증
        if (request.getGolfCourseIds() == null || request.getGolfCourseIds().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CommonResponse.<List<CourseRecommendationResponseDto>>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .msg("골프장 정보가 필요합니다.")
                            .build()
            );
        }

        if (request.getTeeOffTimes() == null || request.getTeeOffTimes().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CommonResponse.<List<CourseRecommendationResponseDto>>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .msg("티오프 시간 정보가 필요합니다.")
                            .build()
            );
        }

        if (request.getStartDate() == null || request.getTravelDays() == null) {
            return ResponseEntity.badRequest().body(
                    CommonResponse.<List<CourseRecommendationResponseDto>>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .msg("여행 시작 날짜와 기간 정보가 필요합니다.")
                            .build()
            );
        }

        List<CourseRecommendationResponseDto> recommendations = recommendationService.createMultiDayAiRecommendation(
                userDetails.getUser(),
                request.getGolfCourseIds(),
                request.getTeeOffTimes(),
                request.getCourseType(),
                request.getStartDate(),
                request.getTravelDays(),
                userPreferences
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                CommonResponse.<List<CourseRecommendationResponseDto>>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .msg("AI " + request.getTravelDays() + "일 골프 여행 코스 추천 생성 성공")
                        .data(recommendations)
                        .build()
        );
    }
}