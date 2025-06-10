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

        @JsonProperty("사업장명(대표자)")
        private String golfCourseName;

        @JsonProperty("코스구성(코스명)")
        private String courseType;

        @JsonProperty("코스길이(야드)")
        private String courseLength;

        @JsonProperty("홀수")
        private Integer holeCount;

        @JsonProperty("총면적(㎡)")
        private String totalArea;
    }
}
