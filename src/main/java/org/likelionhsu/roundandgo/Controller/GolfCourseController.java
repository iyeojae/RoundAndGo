package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Dto.Response.GolfCourseResponseDto;
import org.likelionhsu.roundandgo.Service.GolfCourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/golf-courses")
public class GolfCourseController {

    private final GolfCourseService golfCourseService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<GolfCourseResponseDto>>> getAll() {
        List<GolfCourseResponseDto> list = golfCourseService.getAllGolfCourses();
        return ResponseEntity.ok(
                CommonResponse.<List<GolfCourseResponseDto>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("전체 골프장 조회 성공")
                        .data(list)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<GolfCourseResponseDto>> getById(@PathVariable Long id) {
        GolfCourseResponseDto dto = golfCourseService.getGolfCourseById(id);
        return ResponseEntity.ok(
                CommonResponse.<GolfCourseResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("골프장 상세 조회 성공")
                        .data(dto)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<CommonResponse<List<GolfCourseResponseDto>>> searchByName(@RequestParam("name") String name) {
        List<GolfCourseResponseDto> list = golfCourseService.searchGolfCoursesByName(name);
        return ResponseEntity.ok(
                CommonResponse.<List<GolfCourseResponseDto>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("골프장명 검색 성공")
                        .data(list)
                        .build()
        );
    }

    @GetMapping("/search-by-address")
    public ResponseEntity<CommonResponse<List<GolfCourseResponseDto>>> searchByAddress(@RequestParam("address") String address) {
        List<GolfCourseResponseDto> list = golfCourseService.searchGolfCoursesByAddress(address);
        return ResponseEntity.ok(
                CommonResponse.<List<GolfCourseResponseDto>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("골프장 주소 검색 성공")
                        .data(list)
                        .build()
        );
    }

    // 수동으로 골프장 정보 동기화 배포 시 무조건 제거
    @PostMapping("/sync")
    public ResponseEntity<CommonResponse<String>> manualSync() {
        golfCourseService.updateGolfCourses();
        return ResponseEntity.ok(
                CommonResponse.<String>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("골프장 정보 수동 동기화 완료")
                        .data("done")
                        .build()
        );
    }
}
