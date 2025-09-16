package org.likelionhsu.roundandgo.Dto.Api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedPlaceDto {
    private String type;       // food / tour / stay
    private String name;
    private String address;
    private String imageUrl;
    private double distanceKm;
    private String mapx;
    private String mapy;

    // 시간 정보 추가
    private String startTime;  // 활동 시작 시간
    private String endTime;    // 활동 종료 시간
    private String duration;   // 소요 시간 (예: "1시간 30분")
    private String timeLabel;  // 시간대 라벨 (예: "점심 시간", "관광 시간", "숙소 도착")
}