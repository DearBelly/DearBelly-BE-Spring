package com.hanium.mom4u.domain.calendar.repository;

import com.hanium.mom4u.domain.calendar.entity.Schedule;
import com.hanium.mom4u.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByMemberAndStartDate(Member member, LocalDate date);
    List<Schedule> findAllByMemberIdAndStartDateBetween(Long memberId, LocalDate start, LocalDate end);

    long countByMemberAndStartDate(Member member, LocalDate startDate);


}
