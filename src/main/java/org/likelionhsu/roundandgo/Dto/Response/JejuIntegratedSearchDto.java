package org.likelionhsu.roundandgo.Dto.Response;

import lombok.Builder;
import lombok.Data;
import org.likelionhsu.roundandgo.Dto.Api.TourItem;

import java.util.List;

@Data
@Builder
public class JejuIntegratedSearchDto {
    private List<TourItem> allResults;
    private int totalCount;
    private int attractionCount;
    private int restaurantCount;
    private int accommodationCount;
}
