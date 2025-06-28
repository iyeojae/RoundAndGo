package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.Exception.CustomException;
import org.likelionhsu.roundandgo.Common.Exception.ErrorCode;
import org.likelionhsu.roundandgo.Dto.CultureGolfDto;
import org.likelionhsu.roundandgo.Dto.GolfCourseResponseDto;
import org.likelionhsu.roundandgo.Dto.TourApiGolfDto;
import org.likelionhsu.roundandgo.Entity.GolfCourse;
import org.likelionhsu.roundandgo.ExternalApi.CultureApiClient;
import org.likelionhsu.roundandgo.ExternalApi.TourApiClient;
import org.likelionhsu.roundandgo.Mapper.GolfCourseMapper;
import org.likelionhsu.roundandgo.Repository.GolfCourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GolfCourseService {

    private final TourApiClient tourApiClient;
    private final CultureApiClient cultureApiClient;
    private final GolfCourseRepository golfCourseRepository;

    @Transactional
    public void updateGolfCourses() {
        List<TourApiGolfDto> tourList = tourApiClient.fetchGolfList();

        for (TourApiGolfDto tourDto : tourList) {
            CultureGolfDto cultureDto = cultureApiClient.findByAddress(tourDto.getAddr1(), tourDto.getTitle());

            GolfCourse course = GolfCourseMapper.toEntity(tourDto, cultureDto);

            golfCourseRepository.findByContentId(course.getContentId())
                    .ifPresentOrElse(
                            existing -> {
                                course.setId(existing.getId());
                                golfCourseRepository.save(course);
                            },
                            () -> golfCourseRepository.save(course)
                    );
        }
    }

    @Transactional(readOnly = true)
    public List<GolfCourseResponseDto> getAllGolfCourses() {
        return golfCourseRepository.findAll().stream()
                .map(GolfCourseMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GolfCourseResponseDto getGolfCourseById(Long id) {
        GolfCourse course = golfCourseRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GOLF_COURSE_NOT_FOUND));
        return GolfCourseMapper.toResponseDto(course);
    }

    @Transactional(readOnly = true)
    public List<GolfCourseResponseDto> searchGolfCoursesByName(String name) {
        String normalizedSearch = normalizeGolfName(name); // 검색어도 정규화
        return golfCourseRepository.findAll().stream()
                .filter(course -> isGolfNameMatch(course.getName(), normalizedSearch))
                .map(GolfCourseMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private boolean isGolfNameMatch(String name1, String name2) {
        if (name1 == null || name2 == null) return false;

        String n1 = normalizeGolfName(name1);
        String n2 = normalizeGolfName(name2);

        // 1. 원본 비교
        if (n1.contains(n2)) return true;
        // 2. CC → 컨트리클럽 비교
        if (normalizeGolfName(name1.replace("CC", "컨트리클럽")).contains(normalizeGolfName(name2.replace("CC", "컨트리클럽")))) return true;
        // 3. 컨트리클럽 → CC 비교
        if (normalizeGolfName(name1.replace("컨트리클럽", "CC")).contains(normalizeGolfName(name2.replace("컨트리클럽", "CC")))) return true;

        // (추가: country club 변환도 여기에...)

        return false;
    }

    private String normalizeGolfName(String name) {
        if (name == null) return "";
        // 1. 괄호 및 괄호 안 내용 제거
        String result = name.replaceAll("\\(.*?\\)", "");
        // 2. 공백/특수문자/소문자 통일
        result = result.replaceAll("\\s+", "")
                .replaceAll("[()]", "")
                .toLowerCase();
        return result;
    }
}