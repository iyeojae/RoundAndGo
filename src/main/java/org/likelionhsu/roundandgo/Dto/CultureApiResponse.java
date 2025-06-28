package org.likelionhsu.roundandgo.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CultureApiResponse {

    @JsonProperty("data")
    private List<Item> data;

    @Data
    public static class Item {
        @JsonProperty("소재지")
        private String addr;

        @JsonProperty("업소명")
        private String title;

        @JsonProperty("세부종류")
        private String courseType;

        @JsonProperty("홀수(홀)")
        private Integer holeCount;

        @JsonProperty("총면적(제곱미터)")
        private String totalArea;
    }
}
