package org.likelionhsu.roundandgo.Dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GolfCourseResponseDto {
    private String name;
    private String address;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String courseType;
    private String courseLength;
    private Integer holeCount;
    private String totalArea;
}
