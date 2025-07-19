package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Dto.TourInfoResponseDto;
import org.likelionhsu.roundandgo.Dto.TourItem;
import org.likelionhsu.roundandgo.ExternalApi.TourApiClient;
import org.likelionhsu.roundandgo.Mapper.RegionCodeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourInfoService {

    private final TourApiClient tourApiClient;
    private final RegionCodeMapper regionCodeMapper; // 지역명 -> 코드 매핑 클래스

    /**
     * 특정 지역의 관광 정보(관광지, 숙소, 음식점)를 조회합니다.
     *
     * @param province 도/광역시 이름
     * @param city     시/군/구 이름
     * @return 관광 정보 DTO
     */
    public TourInfoResponseDto getTourInfos(String province, String city) {
        int areaCode = regionCodeMapper.getAreaCode(province);
        int sigunguCode = regionCodeMapper.getSigunguCode(province, city);

        List<TourItem> attractions = tourApiClient.fetchByContentTypes(areaCode, sigunguCode, List.of(12, 14, 15));
        List<TourItem> accommodations = tourApiClient.fetchByContentTypes(areaCode, sigunguCode, List.of(32));
        List<TourItem> restaurants = tourApiClient.fetchByContentTypes(areaCode, sigunguCode, List.of(39));

        return TourInfoResponseDto.builder()
                .attractions(attractions)
                .accommodations(accommodations)
                .restaurants(restaurants)
                .build();
    }

    /**
     * 특정 지역의 관광지 정보를 조회합니다.
     *
     * @param province 도/광역시 이름
     * @param city     시/군/구 이름
     * @return 관광지 리스트
     */
    public List<TourItem> fetchTourAttractions(String province, String city) {
        int areaCode = regionCodeMapper.getAreaCode(province);
        int sigunguCode = regionCodeMapper.getSigunguCode(province, city);

        return tourApiClient.fetchByContentTypes(areaCode, sigunguCode, List.of(12, 14, 15)); // 관광지
    }

    /**
     * 특정 지역의 음식점 정보를 조회합니다.
     *
     * @param province 도/광역시 이름
     * @param city     시/군/구 이름
     * @return 음식점 리스트
     */
    public List<TourItem> fetchRestaurants(String province, String city) {
        int areaCode = regionCodeMapper.getAreaCode(province);
        int sigunguCode = regionCodeMapper.getSigunguCode(province, city);
        return tourApiClient.fetchByContentTypes(areaCode, sigunguCode, List.of(39)); // 음식점
    }

    /**
     * 특정 지역의 숙소 정보를 조회합니다.
     *
     * @param province 도/광역시 이름
     * @param city     시/군/구 이름
     * @return 숙소 리스트
     */
    public List<TourItem> fetchAccommodations(String province, String city) {
        int areaCode = regionCodeMapper.getAreaCode(province);
        int sigunguCode = regionCodeMapper.getSigunguCode(province, city);
        return tourApiClient.fetchByContentTypes(areaCode, sigunguCode, List.of(32)); // 숙소
    }

    /**
     * 현재 위치를 기준으로 반경 내 관광지, 숙소, 음식점을 조회합니다.
     *
     * @param mapX 현재 위치의 X 좌표
     * @param mapY 현재 위치의 Y 좌표
     * @return 반경 내 관광 정보 DTO
     */
    public TourInfoResponseDto fetchNearbyItems(double mapX, double mapY) {
        List<TourItem> attractions = tourApiClient.fetchNearbyItems(mapX, mapY, List.of(12, 14, 15));
        List<TourItem> accommodations = tourApiClient.fetchNearbyItems(mapX, mapY, List.of(32));
        List<TourItem> restaurants = tourApiClient.fetchNearbyItems(mapX, mapY, List.of(39));

        return TourInfoResponseDto.builder()
                .attractions(attractions)
                .accommodations(accommodations)
                .restaurants(restaurants)
                .build();
    }

    /**
     * 현재 위치를 기준으로 반경 내 관광지 정보를 조회합니다.
     *
     * @param mapX 현재 위치의 X 좌표
     * @param mapY 현재 위치의 Y 좌표
     * @return 관광지 리스트
     */
    public List<TourItem> fetchNearByAttractions(double mapX, double mapY) {
        return tourApiClient.fetchNearbyItems(mapX, mapY, List.of(12, 14, 15));// 관광지
    }

    /**
     * 현재 위치를 기준으로 반경 내 음식점 정보를 조회합니다.
     *
     * @param mapX 현재 위치의 X 좌표
     * @param mapY 현재 위치의 Y 좌표
     * @return 음식점 리스트
     */
    public List<TourItem> fetchNearByRestaurants(double mapX, double mapY) {
        return tourApiClient.fetchNearbyItems(mapX, mapY, List.of(32)); // 음식점
    }

    /**
     * 현재 위치를 기준으로 반경 내 숙소 정보를 조회합니다.
     *
     * @param mapX 현재 위치의 X 좌표
     * @param mapY 현재 위치의 Y 좌표
     * @return 숙소 리스트
     */
    public List<TourItem> fetchNearByAccommodations(double mapX, double mapY) {
        return tourApiClient.fetchNearbyItems(mapX, mapY, List.of(39)); // 숙소
    }
}
