package org.likelionhsu.roundandgo.Dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GolfCourseResponseDto {
    private Long id; // 골프장 ID 추가
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
