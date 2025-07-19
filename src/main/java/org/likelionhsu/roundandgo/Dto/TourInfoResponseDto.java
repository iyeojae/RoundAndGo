package org.likelionhsu.roundandgo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.likelionhsu.roundandgo.Dto.TourItem;

import java.util.List;

@Data
@AllArgsConstructor
public class TourInfoResponseDto {
    private List<TourItem> attractions;
    private List<TourItem> accommodations;
    private List<TourItem> restaurants;
}