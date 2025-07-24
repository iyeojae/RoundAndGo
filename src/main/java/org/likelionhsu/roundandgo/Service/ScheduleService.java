package org.likelionhsu.roundandgo.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Dto.Request.ScheduleRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.ScheduleResponseDto;
import org.likelionhsu.roundandgo.Entity.Schedule;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.Repository.ScheduleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleResponseDto createSchedule(User user, ScheduleRequestDto dto,
                                              String startDateTime,
                                              String endDateTime) {
        Schedule schedule = new Schedule();
        schedule.setTitle(dto.getTitle());
        schedule.setStartDateTime(startDateTime);
        schedule.setEndDateTime(endDateTime);
        schedule.setAllDay(dto.isAllDay());
        schedule.setColor(dto.getColor());
        schedule.setCategory(dto.getCategory());
        schedule.setLocation(dto.getLocation());
        schedule.setUser(user);

        Schedule saved = scheduleRepository.save(schedule);
        return toResponseDto(saved);
    }

    public List<ScheduleResponseDto> getAllSchedules(User user) {
        return scheduleRepository.findAllByUserOrderByStartDateTimeAsc(user)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public ScheduleResponseDto getSchedule(User user, Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("일정이 존재하지 않습니다."));

        if (!schedule.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 일정에 접근할 권한이 없습니다.");
        }
        return toResponseDto(schedule);
    }

    public ScheduleResponseDto updateSchedule(User user, Long id, ScheduleRequestDto dto,
                                              String startDateTime,
                                              String endDateTime) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("일정이 존재하지 않습니다."));

        if (!schedule.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 일정에 접근할 권한이 없습니다.");
        }

        schedule.setTitle(dto.getTitle());
        schedule.setStartDateTime(startDateTime);
        schedule.setEndDateTime(endDateTime);
        schedule.setAllDay(dto.isAllDay());
        schedule.setColor(dto.getColor());
        schedule.setCategory(dto.getCategory());
        schedule.setLocation(dto.getLocation());

        Schedule updated = scheduleRepository.save(schedule);
        return toResponseDto(updated);
    }

    public void deleteSchedule(User user, Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("일정이 존재하지 않습니다."));

        if (!schedule.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 일정에 접근할 권한이 없습니다.");
        }
        scheduleRepository.delete(schedule);
    }

    private ScheduleResponseDto toResponseDto(Schedule schedule) {
        return new ScheduleResponseDto(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getStartDateTime(),
                schedule.getEndDateTime(),
                schedule.isAllDay(),
                schedule.getColor(),
                schedule.getCategory(),
                schedule.getLocation()
        );
    }
}
