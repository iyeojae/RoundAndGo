package org.likelionhsu.roundandgo.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.likelionhsu.roundandgo.Common.ScheduleColor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ScheduleResponseDto {
    private Long id;
    private String title;
    private String startDateTime;
    private String endDateTime;
    private boolean allDay;
    private ScheduleColor color;
    private String category;
    private String location;
}
