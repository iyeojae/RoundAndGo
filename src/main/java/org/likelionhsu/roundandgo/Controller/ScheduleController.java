package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Dto.Request.ScheduleRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.ScheduleResponseDto;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 일정 추가
    @PostMapping
    public ResponseEntity<CommonResponse<ScheduleResponseDto>> createSchedule(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ScheduleRequestDto dto,
            @RequestParam String startDateTime,
            @RequestParam String endDateTime) {
        ScheduleResponseDto response = scheduleService.createSchedule(userDetails.getUser(), dto, startDateTime, endDateTime);

        return ResponseEntity.ok(CommonResponse.<ScheduleResponseDto>builder()
                .statusCode(200)
                .msg("일정 추가 완료")
                .data(response)
                .build());
    }

    // 일정 전체 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<ScheduleResponseDto>>> getSchedules(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ScheduleResponseDto> schedules = scheduleService.getAllSchedules(userDetails.getUser());

        return ResponseEntity.ok(CommonResponse.<List<ScheduleResponseDto>>builder()
                .statusCode(200)
                .msg("일정 조회 완료")
                .data(schedules)
                .build());
    }

    // 일정 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ScheduleResponseDto>> getSchedule(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {

        ScheduleResponseDto schedule = scheduleService.getSchedule(userDetails.getUser(), id);

        return ResponseEntity.ok(CommonResponse.<ScheduleResponseDto>builder()
                .statusCode(200)
                .msg("일정 단건 조회 완료")
                .data(schedule)
                .build());
    }

    // 일정 수정
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<ScheduleResponseDto>> updateSchedule(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id,
            @RequestBody ScheduleRequestDto dto,
            @RequestParam String startDateTime,
            @RequestParam String endDateTime) {

        ScheduleResponseDto updated = scheduleService.updateSchedule(userDetails.getUser(), id, dto, startDateTime, endDateTime);

        return ResponseEntity.ok(CommonResponse.<ScheduleResponseDto>builder()
                .statusCode(200)
                .msg("일정 수정 완료")
                .data(updated)
                .build());
    }

    // 일정 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteSchedule(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {

        scheduleService.deleteSchedule(userDetails.getUser(), id);

        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .statusCode(200)
                .msg("일정 삭제 완료")
                .data(null)
                .build());
    }
}