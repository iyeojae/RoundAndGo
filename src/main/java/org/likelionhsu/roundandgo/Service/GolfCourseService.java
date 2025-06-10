package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.Exception.CustomException;
import org.likelionhsu.roundandgo.Common.Exception.ErrorCode;
import org.likelionhsu.roundandgo.Dto.CultureGolfDto;
import org.likelionhsu.roundandgo.Dto.DetailInfoDto;
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
            DetailInfoDto detailDto = tourApiClient.fetchDetailInfo(tourDto.getContentid());
            CultureGolfDto cultureDto = cultureApiClient.findByName(tourDto.getTitle());

            GolfCourse course = GolfCourseMapper.toEntity(tourDto, detailDto, cultureDto);

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
}