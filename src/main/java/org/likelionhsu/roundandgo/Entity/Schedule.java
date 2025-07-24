package org.likelionhsu.roundandgo.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.likelionhsu.roundandgo.Common.ScheduleColor;

import java.time.LocalDateTime;

@Entity
@Data
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String startDateTime;
    private String endDateTime;

    private boolean allDay;

    @Enumerated(EnumType.STRING)
    private ScheduleColor color;

    private String category;
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 사용자 연관관계 추가
}
