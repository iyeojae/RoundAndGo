package org.likelionhsu.roundandgo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.likelionhsu.roundandgo.Common.ScheduleColor;

import java.time.LocalDateTime;

@Getter @Setter
public class ScheduleRequestDto {
    private String title;

    private boolean allDay;
    private ScheduleColor color;
    private String category;
    private String location;
}
