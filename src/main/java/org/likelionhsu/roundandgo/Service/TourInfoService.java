package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Dto.Response.TourInfoResponseDto;
import org.likelionhsu.roundandgo.Dto.Response.JejuIntegratedSearchDto;
import org.likelionhsu.roundandgo.Dto.Api.TourItem;
import org.likelionhsu.roundandgo.Entity.GolfCourse;
import org.likelionhsu.roundandgo.ExternalApi.TourApiClient;
import org.likelionhsu.roundandgo.Mapper.CourseTypeMapper;
import org.likelionhsu.roundandgo.Mapper.RegionCodeMapper;
import org.likelionhsu.roundandgo.Repository.GolfCourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourInfoService {

    private final TourApiClient tourApiClient;
    private final RegionCodeMapper regionCodeMapper;
    private final GolfCourseRepository golfCourseRepository;

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
        return tourApiClient.fetchNearbyItems(mapX, mapY, List.of(39)); // 음식점
    }

    /**
     * 현재 위치를 기준으로 반경 내 숙소 정보를 조회합니다.
     *
     * @param mapX 현재 위치의 X 좌표
     * @param mapY 현재 위치의 Y 좌표
     * @return 숙소 리스트
     */
    public List<TourItem> fetchNearByAccommodations(double mapX, double mapY) {
        return tourApiClient.fetchNearbyItems(mapX, mapY, List.of(32)); // 숙소
    }

    public List<TourItem> fetchAccommodationsByCourseType(String province, String city, String courseType) {
        List<TourItem> all = fetchAccommodations(province, city);
        List<String> cat3Filter = CourseTypeMapper.getCat3Codes(courseType);

        return all.stream()
                .filter(item -> cat3Filter.contains(item.getCat3()))
                .toList();
    }

    public List<TourItem> fetchNearByAccommodationsByCourseType(double mapX, double mapY, String courseType) {
        List<TourItem> all = fetchNearByAccommodations(mapX, mapY);
        List<String> cat3Filter = CourseTypeMapper.getCat3Codes(courseType);

        return all.stream()
                .filter(item -> cat3Filter.contains(item.getCat3()))
                .toList();
    }

    /**
     * 골프장 ID를 기반으로 해당 지역의 전체 관광 정보를 조회합니다.
     *
     * @param golfCourseId 골프장 ID
     * @return 관광 정보 DTO
     */
    public TourInfoResponseDto getTourInfosByGolfCourse(Long golfCourseId) {
        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장을 찾을 수 없습니다."));

        // 골프장 주소를 파싱하여 지역 정보 추출
        String[] addressParts = parseAddress(golfCourse.getAddress());
        String province = addressParts[0];
        String city = addressParts[1];

        return getTourInfos(province, city);
    }

    /**
     * 골프장 ID를 기반으로 해당 지역의 관광지를 조회합니다.
     *
     * @param golfCourseId 골프장 ID
     * @return 관광지 리스트
     */
    public List<TourItem> fetchTourAttractionsByGolfCourse(Long golfCourseId) {
        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장을 찾을 수 없습니다."));

        String[] addressParts = parseAddress(golfCourse.getAddress());
        String province = addressParts[0];
        String city = addressParts[1];

        return fetchTourAttractions(province, city);
    }

    /**
     * 골프장 ID를 기반으로 해당 지역의 음식점을 조회합니다.
     *
     * @param golfCourseId 골프장 ID
     * @return 음식점 리스트
     */
    public List<TourItem> fetchRestaurantsByGolfCourse(Long golfCourseId) {
        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장을 찾을 수 없습니다."));

        String[] addressParts = parseAddress(golfCourse.getAddress());
        String province = addressParts[0];
        String city = addressParts[1];

        return fetchRestaurants(province, city);
    }

    /**
     * 골프장 ID를 기반으로 해당 지역의 숙소를 조회합니다.
     *
     * @param golfCourseId 골프장 ID
     * @return 숙소 리스트
     */
    public List<TourItem> fetchAccommodationsByGolfCourse(Long golfCourseId) {
        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장을 찾을 수 없습니다."));

        String[] addressParts = parseAddress(golfCourse.getAddress());
        String province = addressParts[0];
        String city = addressParts[1];

        return fetchAccommodations(province, city);
    }

    /**
     * 골프장 ID를 기반으로 골프장 좌표 주변의 전체 관광 정보를 조회합니다.
     *
     * @param golfCourseId 골프장 ID
     * @return 관광 정보 DTO
     */
    public TourInfoResponseDto getNearbyItemsByGolfCourse(Long golfCourseId) {
        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장을 찾을 수 없습니다."));

        return fetchNearbyItems(golfCourse.getLongitude(), golfCourse.getLatitude());
    }

    /**
     * 골프장 ID를 기반으로 골프장 좌표 주변의 관광지를 조회합니다.
     *
     * @param golfCourseId 골프장 ID
     * @return 관광지 리스트
     */
    public List<TourItem> fetchNearByAttractionsByGolfCourse(Long golfCourseId) {
        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장을 찾을 수 없습니다."));

        return fetchNearByAttractions(golfCourse.getLongitude(), golfCourse.getLatitude());
    }

    /**
     * 골프장 ID를 기반으로 골프장 좌표 주변의 음식점을 조회합니다.
     *
     * @param golfCourseId 골프장 ID
     * @return 음식점 리스트
     */
    public List<TourItem> fetchNearByRestaurantsByGolfCourse(Long golfCourseId) {
        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장을 찾을 수 없습니다."));

        return fetchNearByRestaurants(golfCourse.getLongitude(), golfCourse.getLatitude());
    }

    /**
     * 골프장 ID를 기반으로 골프장 좌표 주변의 숙소를 조회합니다.
     *
     * @param golfCourseId 골프장 ID
     * @return 숙소 리스트
     */
    public List<TourItem> fetchNearByAccommodationsByGolfCourse(Long golfCourseId) {
        GolfCourse golfCourse = golfCourseRepository.findById(golfCourseId)
                .orElseThrow(() -> new RuntimeException("골프장을 찾을 수 없습니다."));

        return fetchNearByAccommodations(golfCourse.getLongitude(), golfCourse.getLatitude());
    }

    /**
     * 주소를 파싱하여 도/광역시와 시/군/구를 추출합니다.
     *
     * @param address 전체 주소
     * @return [도/광역시, 시/군/구] 배열
     */
    private String[] parseAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new RuntimeException("주소 정보가 없습니다.");
        }

        String[] parts = address.split(" ");
        if (parts.length < 2) {
            throw new RuntimeException("주소 형식이 올바르지 않습니다.");
        }

        String province = parts[0];
        String city = parts[1];

        return new String[]{province, city};
    }

    /**
     * 제주도의 관광지, 음식점, 숙소를 통합 검색합니다.
     *
     * @param keyword 검색 키워드 (선택사항)
     * @return 제주도 통합 검색 결과
     */
    public JejuIntegratedSearchDto searchJejuIntegrated(String keyword) {
        // 제주도 지역 코드: 39
        int jejuAreaCode = 39;

        List<TourItem> attractions = tourApiClient.fetchByContentTypes(jejuAreaCode, 0, List.of(12, 14, 15));
        List<TourItem> restaurants = tourApiClient.fetchByContentTypes(jejuAreaCode, 0, List.of(39));
        List<TourItem> accommodations = tourApiClient.fetchByContentTypes(jejuAreaCode, 0, List.of(32));

        // 모든 결과를 하나의 리스트로 통합
        List<TourItem> allResults = new java.util.ArrayList<>();
        allResults.addAll(attractions);
        allResults.addAll(restaurants);
        allResults.addAll(accommodations);

        // 키워드가 있는 경우 필터링
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            allResults = allResults.stream()
                    .filter(item -> item.getTitle() != null &&
                                  item.getTitle().toLowerCase().contains(lowerKeyword))
                    .toList();
        }

        return JejuIntegratedSearchDto.builder()
                .allResults(allResults)
                .totalCount(allResults.size())
                .attractionCount((int) allResults.stream().filter(item ->
                    item.getContenttypeid() == 12 || item.getContenttypeid() == 14 || item.getContenttypeid() == 15).count())
                .restaurantCount((int) allResults.stream().filter(item -> item.getContenttypeid() == 39).count())
                .accommodationCount((int) allResults.stream().filter(item -> item.getContenttypeid() == 32).count())
                .build();
    }

    /**
     * 제주도의 관광지를 키워드로 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색된 관광지 리스트
     */
    public List<TourItem> searchJejuAttractions(String keyword) {
        // 제주도 지역 코드: 39
        int jejuAreaCode = 39;

        List<TourItem> attractions = tourApiClient.fetchByContentTypes(jejuAreaCode, 0, List.of(12, 14, 15));

        // 키워드가 있는 경우 필터링
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            attractions = attractions.stream()
                    .filter(item -> item.getTitle() != null &&
                                  item.getTitle().toLowerCase().contains(lowerKeyword))
                    .toList();
        }

        return attractions;
    }

    /**
     * 제주도의 음식점을 키워드로 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색된 음식점 리스트
     */
    public List<TourItem> searchJejuRestaurants(String keyword) {
        // 제주도 지역 코드: 39
        int jejuAreaCode = 39;

        List<TourItem> restaurants = tourApiClient.fetchByContentTypes(jejuAreaCode, 0, List.of(39));

        // 키워드가 있는 경우 필터링
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            restaurants = restaurants.stream()
                    .filter(item -> item.getTitle() != null &&
                                  item.getTitle().toLowerCase().contains(lowerKeyword))
                    .toList();
        }

        return restaurants;
    }

    /**
     * 제주도의 숙소를 키워드로 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색된 숙소 리스트
     */
    public List<TourItem> searchJejuAccommodations(String keyword) {
        // 제주도 지역 코드: 39
        int jejuAreaCode = 39;

        List<TourItem> accommodations = tourApiClient.fetchByContentTypes(jejuAreaCode, 0, List.of(32));

        // 키워드가 있는 경우 필터링
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            accommodations = accommodations.stream()
                    .filter(item -> item.getTitle() != null &&
                                  item.getTitle().toLowerCase().contains(lowerKeyword))
                    .toList();
        }

        return accommodations;
    }
}
