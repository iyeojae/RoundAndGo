package org.likelionhsu.roundandgo.Dto.Response;

import lombok.Builder;
import lombok.Data;
import org.likelionhsu.roundandgo.Dto.Api.TourItem;

import java.util.List;

@Data
@Builder
public class TourInfoResponseDto {
    private List<TourItem> attractions;
    private List<TourItem> accommodations;
    private List<TourItem> restaurants;
}