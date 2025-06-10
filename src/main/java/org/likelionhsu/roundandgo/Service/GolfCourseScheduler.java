package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GolfCourseScheduler {

    private final GolfCourseService golfCourseService;

    @Scheduled(cron = "0 0 3 * * MON") // 매주 월요일 새벽 3시
    public void updateGolfCourseDataWeekly() {
        golfCourseService.updateGolfCourses();
        System.out.println("[스케줄러] 골프장 정보가 주간 갱신되었습니다.");
    }
}
