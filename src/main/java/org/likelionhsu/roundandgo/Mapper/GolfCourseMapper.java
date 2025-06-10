package org.likelionhsu.roundandgo.Mapper;

import org.likelionhsu.roundandgo.Dto.CultureGolfDto;
import org.likelionhsu.roundandgo.Dto.DetailInfoDto;
import org.likelionhsu.roundandgo.Dto.GolfCourseResponseDto;
import org.likelionhsu.roundandgo.Dto.TourApiGolfDto;
import org.likelionhsu.roundandgo.Entity.GolfCourse;

public class GolfCourseMapper {
    public static GolfCourse toEntity(
            TourApiGolfDto tourDto,
            DetailInfoDto detailDto,
            CultureGolfDto cultureDto) {

        GolfCourse course = new GolfCourse();
        course.setName(tourDto.getTitle());
        course.setAddress(tourDto.getAddr1());
        course.setPhoneNumber(tourDto.getTel());
        course.setLatitude(Double.parseDouble(tourDto.getMapy()));
        course.setLongitude(Double.parseDouble(tourDto.getMapx()));
        course.setImageUrl(tourDto.getFirstimage());
        course.setContentId(tourDto.getContentid());

        if (detailDto != null) {
            course.setFeeInfo(detailDto.getFeeInfo());
        }

        if (cultureDto != null) {
            course.setCourseType(cultureDto.getCourseType());
            course.setCourseLength(cultureDto.getCourseLength());
            course.setHoleCount(cultureDto.getHoleCount());
            course.setTotalArea(cultureDto.getTotalArea());
        }

        return course;
    }

    public static GolfCourseResponseDto toResponseDto(GolfCourse entity) {
        return GolfCourseResponseDto.builder()
                .name(entity.getName())
                .address(entity.getAddress())
                .phoneNumber(entity.getPhoneNumber())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .imageUrl(entity.getImageUrl())
                .feeInfo(entity.getFeeInfo())
                .courseType(entity.getCourseType())
                .courseLength(entity.getCourseLength())
                .holeCount(entity.getHoleCount())
                .totalArea(entity.getTotalArea())
                .build();
    }
}
