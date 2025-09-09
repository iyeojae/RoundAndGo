package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Dto.Request.SavedCourseRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.SavedCourseResponseDto;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Service.SavedCourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/saved")
public class SavedCourseController {

    private final SavedCourseService savedCourseService;

    // 코스 저장
    @PostMapping
    public ResponseEntity<CommonResponse<SavedCourseResponseDto>> saveCourse(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody SavedCourseRequestDto request
    ) {
        SavedCourseResponseDto dto = savedCourseService.saveCourse(userDetails.getUser(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                CommonResponse.<SavedCourseResponseDto>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .msg("코스 저장 성공")
                        .data(dto)
                        .build()
        );
    }

    // 내 저장된 코스 목록 조회
    @GetMapping("/my")
    public ResponseEntity<CommonResponse<List<SavedCourseResponseDto>>> getMyCourses(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<SavedCourseResponseDto> courses = savedCourseService.getMyCourses(userDetails.getUser());

        return ResponseEntity.ok(
                CommonResponse.<List<SavedCourseResponseDto>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("내 저장된 코스 조회 성공")
                        .data(courses)
                        .build()
        );
    }

    // 공개된 코스 목록 조회
    @GetMapping("/public")
    public ResponseEntity<CommonResponse<List<SavedCourseResponseDto>>> getPublicCourses() {
        List<SavedCourseResponseDto> courses = savedCourseService.getPublicCourses();

        return ResponseEntity.ok(
                CommonResponse.<List<SavedCourseResponseDto>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("공개 코스 조회 성공")
                        .data(courses)
                        .build()
        );
    }

    // 코스 타입별 공개 코스 조회
    @GetMapping("/public/type/{courseType}")
    public ResponseEntity<CommonResponse<List<SavedCourseResponseDto>>> getPublicCoursesByType(
            @PathVariable String courseType
    ) {
        List<SavedCourseResponseDto> courses = savedCourseService.getPublicCoursesByType(courseType);

        return ResponseEntity.ok(
                CommonResponse.<List<SavedCourseResponseDto>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg(courseType + " 타입 공개 코스 조회 성공")
                        .data(courses)
                        .build()
        );
    }

    // 단건 코스 조회
    @GetMapping("/{courseId}")
    public ResponseEntity<CommonResponse<SavedCourseResponseDto>> getCourse(
            @PathVariable Long courseId
    ) {
        SavedCourseResponseDto course = savedCourseService.getCourse(courseId);

        return ResponseEntity.ok(
                CommonResponse.<SavedCourseResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("코스 조회 성공")
                        .data(course)
                        .build()
        );
    }

    // 코스 수정
    @PutMapping("/{courseId}")
    public ResponseEntity<CommonResponse<SavedCourseResponseDto>> updateCourse(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long courseId,
            @RequestBody SavedCourseRequestDto request
    ) {
        SavedCourseResponseDto dto = savedCourseService.updateCourse(userDetails.getUser(), courseId, request);

        return ResponseEntity.ok(
                CommonResponse.<SavedCourseResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("코스 수정 성공")
                        .data(dto)
                        .build()
        );
    }

    // 코스 삭제
    @DeleteMapping("/{courseId}")
    public ResponseEntity<CommonResponse<Void>> deleteCourse(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long courseId
    ) {
        savedCourseService.deleteCourse(userDetails.getUser(), courseId);

        return ResponseEntity.ok(
                CommonResponse.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("코스 삭제 성공")
                        .build()
        );
    }
}
