package org.likelionhsu.roundandgo.Mapper;

import org.likelionhsu.roundandgo.Dto.Api.CultureGolfDto;
import org.likelionhsu.roundandgo.Dto.Response.GolfCourseResponseDto;
import org.likelionhsu.roundandgo.Dto.Api.TourApiGolfDto;
import org.likelionhsu.roundandgo.Entity.GolfCourse;

public class GolfCourseMapper {
    public static GolfCourse toEntity(
            TourApiGolfDto tourDto,
            CultureGolfDto cultureDto) {

        GolfCourse course = new GolfCourse();
        course.setName(tourDto.getTitle());
        course.setAddress(tourDto.getAddr1());
        course.setPhoneNumber(tourDto.getTel());
        course.setLatitude(Double.parseDouble(tourDto.getMapy()));
        course.setLongitude(Double.parseDouble(tourDto.getMapx()));
        course.setImageUrl(tourDto.getFirstimage());
        course.setContentId(tourDto.getContentid());

        if (cultureDto != null) {
            course.setCourseType(cultureDto.getCourseType());
            course.setHoleCount(cultureDto.getHoleCount());
            course.setTotalArea(cultureDto.getTotalArea());
        }

        return course;
    }

    public static GolfCourseResponseDto toResponseDto(GolfCourse entity) {
        return GolfCourseResponseDto.builder()
                .id(entity.getId()) // 골프장 ID 매핑 추가
                .name(entity.getName())
                .address(entity.getAddress())
                .phoneNumber(entity.getPhoneNumber())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .imageUrl(entity.getImageUrl())
                .courseType(entity.getCourseType())
                .holeCount(entity.getHoleCount())
                .totalArea(entity.getTotalArea())
                .build();
    }
}
