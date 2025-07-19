package org.likelionhsu.roundandgo.Dto;

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
}