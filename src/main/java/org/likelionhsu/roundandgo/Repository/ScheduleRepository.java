package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.Schedule;
import org.likelionhsu.roundandgo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByUserOrderByStartDateTimeAsc(User user);
}
