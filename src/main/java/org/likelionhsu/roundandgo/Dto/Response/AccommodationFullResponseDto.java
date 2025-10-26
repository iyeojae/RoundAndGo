package org.likelionhsu.roundandgo.Dto.Response;

import lombok.Data;
import org.likelionhsu.roundandgo.Dto.Api.AccommodationDetailDto;
import org.likelionhsu.roundandgo.Dto.Api.AccommodationImageDto;
import org.likelionhsu.roundandgo.Dto.Api.AccommodationInfoDto;

import java.util.List;

@Data
public class AccommodationFullResponseDto {
    private AccommodationDetailDto detail;
    private List<AccommodationImageDto> images;
    private List<AccommodationInfoDto> info;
}
