package org.likelionhsu.roundandgo.Mapper;

import java.util.List;

public class CourseTypeMapper {

    public static List<String> getCat3Codes(String courseType) {
        return switch (courseType) {
            case "luxury" -> List.of("B02010100", "B02011300"); // 관광호텔, 서비스드레지던스
            case "value" -> List.of("B02010900", "B02010600", "B02011100", "B02011000");
            case "resort" -> List.of("B02010500", "B02010700");
            case "theme" -> List.of("B02011600", "B02011200");
            default -> List.of(); // 예외 처리도 가능
        };
    }
}