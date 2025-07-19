package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Dto.TourInfoResponseDto;
import org.likelionhsu.roundandgo.Dto.TourItem;
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
}