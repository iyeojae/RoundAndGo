package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Dto.Api.AccommodationDetailDto;
import org.likelionhsu.roundandgo.Dto.Api.AccommodationImageDto;
import org.likelionhsu.roundandgo.Dto.Api.AccommodationInfoDto;
import org.likelionhsu.roundandgo.ExternalApi.TourApiClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccommodationService {

    private final TourApiClient tourApiClient;

    /**
     * 숙소 상세 정보 조회
     * @param contentId 숙소 ID
     * @return 숙소 상세 정보
     */
    public AccommodationDetailDto getAccommodationDetail(String contentId) {
        try {
            return tourApiClient.fetchAccommodationDetail(contentId);
        } catch (Exception e) {
            log.error("숙소 상세 정보 조회 실패 - contentId: {}, error: {}", contentId, e.getMessage());
            return null;
        }
    }

    /**
     * 숙소 이미지 목록 조회
     * @param contentId 숙소 ID
     * @return 숙소 이미지 목록
     */
    public List<AccommodationImageDto> getAccommodationImages(String contentId) {
        try {
            return tourApiClient.fetchAccommodationImages(contentId);
        } catch (Exception e) {
            log.error("숙소 이미지 조회 실패 - contentId: {}, error: {}", contentId, e.getMessage());
            return List.of();
        }
    }

    /**
     * 숙소 부대시설 정보 조회
     * @param contentId 숙소 ID
     * @return 숙소 부대시설 정보 목록
     */
    public List<AccommodationInfoDto> getAccommodationInfo(String contentId) {
        try {
            return tourApiClient.fetchAccommodationInfo(contentId);
        } catch (Exception e) {
            log.error("숙소 부대시설 정보 조회 실패 - contentId: {}, error: {}", contentId, e.getMessage());
            return List.of();
        }
    }
}
