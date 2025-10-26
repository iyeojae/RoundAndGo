package org.likelionhsu.roundandgo.Dto.Api;

import lombok.Data;

@Data
public class AccommodationInfoDto {
    private String roomtitle;
    private String subfacility;
    private String roomtype;
    private String refundregulation;
    private FacilitiesDto facilities;

    @Data
    public static class FacilitiesDto {
        private boolean tv;
        private boolean pc;
        private boolean internet;
        private boolean refrigerator;
        private boolean sofa;
        private boolean table;
        private boolean hairdryer;
        private boolean bath;
        private boolean bathfacility;
        private boolean aircondition;
    }
}
