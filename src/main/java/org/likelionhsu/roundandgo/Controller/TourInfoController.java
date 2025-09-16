package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Dto.Response.TourInfoResponseDto;
import org.likelionhsu.roundandgo.Dto.Response.JejuIntegratedSearchDto;
import org.likelionhsu.roundandgo.Dto.Api.TourItem;
import org.likelionhsu.roundandgo.Service.TourInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tour-infos")
public class TourInfoController {

    private final TourInfoService tourInfoService;

    @GetMapping
    public ResponseEntity<CommonResponse<TourInfoResponseDto>> getTourInfoByRegion(
            @RequestParam String province,
            @RequestParam String city) {

        TourInfoResponseDto response = tourInfoService.getTourInfos(province, city);

        return ResponseEntity.ok(
                CommonResponse.<TourInfoResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("관광 정보 조회 성공")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/attractions")
    public ResponseEntity<List<TourItem>> fetchTourAttractions(
            @RequestParam String province,
            @RequestParam String city) {
        return ResponseEntity.ok(tourInfoService.fetchTourAttractions(province, city));
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<TourItem>> fetchRestaurants(
            @RequestParam String province,
            @RequestParam String city) {
        return ResponseEntity.ok(tourInfoService.fetchRestaurants(province, city));
    }

    @GetMapping("/accommodations")
    public ResponseEntity<List<TourItem>> fetchAccommodations(
            @RequestParam String province,
            @RequestParam String city) {
        return ResponseEntity.ok(tourInfoService.fetchAccommodations(province, city));
    }

    @GetMapping("/accommodations/by-course-type")
    public ResponseEntity<List<TourItem>> fetchAccommodationsByCourseType(
            @RequestParam String province,
            @RequestParam String city,
            @RequestParam String courseType) {

        return ResponseEntity.ok(tourInfoService.fetchAccommodationsByCourseType(province, city, courseType));
    }

    @GetMapping("/nearby")
    public ResponseEntity<CommonResponse<TourInfoResponseDto>> getNearbyItems(
            @RequestParam double mapX,
            @RequestParam double mapY) {

        TourInfoResponseDto response = tourInfoService.fetchNearbyItems(mapX, mapY);

        return ResponseEntity.ok(
                CommonResponse.<TourInfoResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("반경 내 관광지 조회 성공")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/nearby/attractions")
    public ResponseEntity<List<TourItem>> fetchNearByAttractions(
            @RequestParam double mapX,
            @RequestParam double mapY) {
        return ResponseEntity.ok(tourInfoService.fetchNearByAttractions(mapX, mapY));
    }

    @GetMapping("/nearby/restaurants")
    public ResponseEntity<List<TourItem>> fetchNearByRestaurants(
            @RequestParam double mapX,
            @RequestParam double mapY) {
        return ResponseEntity.ok(tourInfoService.fetchNearByRestaurants(mapX, mapY));
    }

    @GetMapping("/nearby/accommodations")
    public ResponseEntity<List<TourItem>> fetchNearByAccommodations(
            @RequestParam double mapX,
            @RequestParam double mapY) {
        return ResponseEntity.ok(tourInfoService.fetchNearByAccommodations(mapX, mapY));
    }

    @GetMapping("/nearby/accommodations/by-course-type")
    public ResponseEntity<List<TourItem>> fetchNearByAccommodationsByCourseType(
            @RequestParam double mapX,
            @RequestParam double mapY,
            @RequestParam String courseType) {

        return ResponseEntity.ok(tourInfoService.fetchNearByAccommodationsByCourseType(mapX, mapY, courseType));
    }

    // 골프장 ID 기반 지역 관광지 조회 엔드포인트들
    @GetMapping("/by-golf-course")
    public ResponseEntity<CommonResponse<TourInfoResponseDto>> getTourInfoByGolfCourse(
            @RequestParam Long golfCourseId) {

        TourInfoResponseDto response = tourInfoService.getTourInfosByGolfCourse(golfCourseId);

        return ResponseEntity.ok(
                CommonResponse.<TourInfoResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("골프장 지역 관광 정보 조회 성공")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/by-golf-course/attractions")
    public ResponseEntity<List<TourItem>> fetchTourAttractionsByGolfCourse(
            @RequestParam Long golfCourseId) {
        return ResponseEntity.ok(tourInfoService.fetchTourAttractionsByGolfCourse(golfCourseId));
    }

    @GetMapping("/by-golf-course/restaurants")
    public ResponseEntity<List<TourItem>> fetchRestaurantsByGolfCourse(
            @RequestParam Long golfCourseId) {
        return ResponseEntity.ok(tourInfoService.fetchRestaurantsByGolfCourse(golfCourseId));
    }

    @GetMapping("/by-golf-course/accommodations")
    public ResponseEntity<List<TourItem>> fetchAccommodationsByGolfCourse(
            @RequestParam Long golfCourseId) {
        return ResponseEntity.ok(tourInfoService.fetchAccommodationsByGolfCourse(golfCourseId));
    }

    // 골프장 ID 기반 좌표 주변 관광지 조회 엔드포인트들
    @GetMapping("/nearby-golf-course")
    public ResponseEntity<CommonResponse<TourInfoResponseDto>> getNearbyItemsByGolfCourse(
            @RequestParam Long golfCourseId) {

        TourInfoResponseDto response = tourInfoService.getNearbyItemsByGolfCourse(golfCourseId);

        return ResponseEntity.ok(
                CommonResponse.<TourInfoResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("골프장 주변 관광 정보 조회 성공")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/nearby-golf-course/attractions")
    public ResponseEntity<List<TourItem>> fetchNearByAttractionsByGolfCourse(
            @RequestParam Long golfCourseId) {
        return ResponseEntity.ok(tourInfoService.fetchNearByAttractionsByGolfCourse(golfCourseId));
    }

    @GetMapping("/nearby-golf-course/restaurants")
    public ResponseEntity<List<TourItem>> fetchNearByRestaurantsByGolfCourse(
            @RequestParam Long golfCourseId) {
        return ResponseEntity.ok(tourInfoService.fetchNearByRestaurantsByGolfCourse(golfCourseId));
    }

    @GetMapping("/nearby-golf-course/accommodations")
    public ResponseEntity<List<TourItem>> fetchNearByAccommodationsByGolfCourse(
            @RequestParam Long golfCourseId) {
        return ResponseEntity.ok(tourInfoService.fetchNearByAccommodationsByGolfCourse(golfCourseId));
    }

    @GetMapping("/jeju/integrated-search")
    public ResponseEntity<CommonResponse<JejuIntegratedSearchDto>> searchJejuIntegrated(
            @RequestParam(required = false) String keyword) {

        JejuIntegratedSearchDto response = tourInfoService.searchJejuIntegrated(keyword);

        return ResponseEntity.ok(
                CommonResponse.<JejuIntegratedSearchDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("제주도 통합 검색 성공")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/jeju/search/attractions")
    public ResponseEntity<CommonResponse<List<TourItem>>> searchJejuAttractions(
            @RequestParam(required = false) String keyword) {

        List<TourItem> attractions = tourInfoService.searchJejuAttractions(keyword);

        return ResponseEntity.ok(
                CommonResponse.<List<TourItem>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("제주도 관광지 검색 성공")
                        .data(attractions)
                        .build()
        );
    }

    @GetMapping("/jeju/search/restaurants")
    public ResponseEntity<CommonResponse<List<TourItem>>> searchJejuRestaurants(
            @RequestParam(required = false) String keyword) {

        List<TourItem> restaurants = tourInfoService.searchJejuRestaurants(keyword);

        return ResponseEntity.ok(
                CommonResponse.<List<TourItem>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("제주도 음식점 검색 성공")
                        .data(restaurants)
                        .build()
        );
    }

    @GetMapping("/jeju/search/accommodations")
    public ResponseEntity<CommonResponse<List<TourItem>>> searchJejuAccommodations(
            @RequestParam(required = false) String keyword) {

        List<TourItem> accommodations = tourInfoService.searchJejuAccommodations(keyword);

        return ResponseEntity.ok(
                CommonResponse.<List<TourItem>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("제주도 숙소 검색 성공")
                        .data(accommodations)
                        .build()
        );
    }
}