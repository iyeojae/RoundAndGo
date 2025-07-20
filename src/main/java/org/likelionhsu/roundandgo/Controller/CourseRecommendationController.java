package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Dto.CourseRecommendationRequestDto;
import org.likelionhsu.roundandgo.Dto.CourseRecommendationResponseDto;
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
}