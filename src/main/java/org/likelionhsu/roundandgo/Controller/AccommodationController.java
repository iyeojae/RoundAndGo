package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Dto.Api.AccommodationDetailDto;
import org.likelionhsu.roundandgo.Dto.Api.AccommodationImageDto;
import org.likelionhsu.roundandgo.Dto.Api.AccommodationInfoDto;
import org.likelionhsu.roundandgo.Dto.Response.AccommodationFullResponseDto;
import org.likelionhsu.roundandgo.Service.AccommodationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accommodation")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AccommodationController {

    private final AccommodationService accommodationService;

    /**
     * 숙소 모든 정보 한 번에 조회 (상세정보, 이미지, 부대시설)
     * @param contentId 숙소 ID
     * @return 숙소 전체 정보
     */
    @GetMapping("/full/{contentId}")
    public ResponseEntity<AccommodationFullResponseDto> getAccommodationFullInfo(@PathVariable String contentId) {
        log.info("숙소 전체 정보 조회 요청 - contentId: {}", contentId);

        AccommodationFullResponseDto response = new AccommodationFullResponseDto();

        // 상세정보 조회
        AccommodationDetailDto detail = accommodationService.getAccommodationDetail(contentId);
        response.setDetail(detail);

        // 이미지 조회
        List<AccommodationImageDto> images = accommodationService.getAccommodationImages(contentId);
        response.setImages(images);

        // 부대시설 정보 조회
        List<AccommodationInfoDto> info = accommodationService.getAccommodationInfo(contentId);
        response.setInfo(info);

        return ResponseEntity.ok(response);
    }

    /**
     * 숙소 상세 정보 조회 (제목, 소개, 주소, 대표 이미지)
     * @param contentId 숙소 ID
     * @return 숙소 상세 정보
     */
    @GetMapping("/detail/{contentId}")
    public ResponseEntity<AccommodationDetailDto> getAccommodationDetail(@PathVariable String contentId) {
        log.info("숙소 상세 정보 조회 요청 - contentId: {}", contentId);

        AccommodationDetailDto detail = accommodationService.getAccommodationDetail(contentId);

        if (detail != null) {
            return ResponseEntity.ok(detail);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 숙소 추가 이미지 목록 조회
     * @param contentId 숙소 ID
     * @return 숙소 이미지 목록
     */
    @GetMapping("/images/{contentId}")
    public ResponseEntity<List<AccommodationImageDto>> getAccommodationImages(@PathVariable String contentId) {
        log.info("숙소 이미지 조회 요청 - contentId: {}", contentId);

        List<AccommodationImageDto> images = accommodationService.getAccommodationImages(contentId);
        return ResponseEntity.ok(images);
    }

    /**
     * 숙소 부대시설 및 서비스 정보 조회
     * @param contentId 숙소 ID
     * @return 숙소 부대시설 정보 목록
     */
    @GetMapping("/info/{contentId}")
    public ResponseEntity<List<AccommodationInfoDto>> getAccommodationInfo(@PathVariable String contentId) {
        log.info("숙소 부대시설 정보 조회 요청 - contentId: {}", contentId);

        List<AccommodationInfoDto> info = accommodationService.getAccommodationInfo(contentId);
        return ResponseEntity.ok(info);
    }
}
