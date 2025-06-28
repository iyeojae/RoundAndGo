package org.likelionhsu.roundandgo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "golf_courses")
@Setter @Getter
public class GolfCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;              // 골프장명
    private String address;           // 주소
    private String phoneNumber;       // 전화번호
    private Double latitude;          // 위도
    private Double longitude;         // 경도
    private String imageUrl;          // 대표 이미지

    @Column(name = "fee_info", columnDefinition = "TEXT")
    private String feeInfo;

    private String courseType;        //  세부정보 (문화체육관광부 API)
    private Integer holeCount;        // 홀 수
    private String totalArea;         // 총 면적

    private String contentId;         // TourAPI contentId (상세정보 조회용)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
