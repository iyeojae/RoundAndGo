package org.likelionhsu.roundandgo.Dto.Api;

import lombok.Data;

@Data
public class TourItem {
    private String title;           // 콘텐츠 명칭
    private String addr1;           // 주소 (기본)
    private String addr2;           // 주소 (상세)
    private String firstimage;      // 대표 이미지 URL
    private String mapx;            // GPS X좌표 (경도)
    private String mapy;            // GPS Y좌표 (위도)
    private int contentid;          // 콘텐츠 ID
    private int contenttypeid;      // 콘텐츠 타입 ID
    private String tel;             // 전화번호
    private String zipcode;         // 우편번호
    private String overview;        // 개요 (상세 조회 시만 나올 수도 있음)
    private String dist;            // 거리 (m 단위) - 좌표 기반 검색 시 반환

    // ✅ 새로 추가
    private String cat1;
    private String cat2;
    private String cat3;
}