package com.hanium.mom4u.domain.calendar.repository;

import com.hanium.mom4u.domain.calendar.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
