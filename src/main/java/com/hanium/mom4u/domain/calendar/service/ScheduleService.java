package com.hanium.mom4u.domain.calendar.service;

import com.hanium.mom4u.domain.calendar.dto.request.ScheduleRequest;
import com.hanium.mom4u.domain.calendar.dto.response.ScheduleResponse;
import com.hanium.mom4u.domain.calendar.entity.Schedule;
import com.hanium.mom4u.domain.calendar.repository.ScheduleRepository;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public List<ScheduleResponse> getSchedulesByMonth(Authentication authentication, int year, int month) {
        Member member = getLoginMember(authentication);
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());

        return scheduleRepository.findAllByMemberAndStartDateBetween(member, start, end)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ScheduleResponse> getSchedulesByDate(Authentication authentication, LocalDate date) {
        Member member = getLoginMember(authentication);
        return scheduleRepository.findAllByMemberAndStartDate(member, date)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ScheduleResponse getScheduleDetail(Long scheduleId, Authentication authentication) {
        Member member = getLoginMember(authentication);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> GeneralException.of(StatusCode.SCHEDULE_NOT_FOUND));

        validateOwnership(schedule, member);
        return toResponse(schedule);
    }

    public void createSchedule(ScheduleRequest request, Authentication authentication) {
        Member member = getLoginMember(authentication);
        // 일정 개수 제한 로직 추가
        long currentCount = scheduleRepository.countByMember(member);
        if (currentCount >= 10) {
            throw GeneralException.of(StatusCode.SCHEDULE_LIMIT_EXCEEDED);
        }
        Schedule schedule = Schedule.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .color(request.getColor())
                .healthCheck(request.getHealthCheck())
                .member(member)
                .build();
        scheduleRepository.save(schedule);
    }

    public void updateSchedule(Long scheduleId, ScheduleRequest request, Authentication authentication) {
        Member member = getLoginMember(authentication);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> GeneralException.of(StatusCode.SCHEDULE_NOT_FOUND));

        validateOwnership(schedule, member);
        schedule.update(
                request.getName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getColor(),
                request.getHealthCheck()
        );
    }

    public void deleteSchedule(Long scheduleId, Authentication authentication) {
        Member member = getLoginMember(authentication);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> GeneralException.of(StatusCode.SCHEDULE_NOT_FOUND));

        validateOwnership(schedule, member);
        scheduleRepository.delete(schedule);
    }

    private void validateOwnership(Schedule schedule, Member member) {
        if (!schedule.getMember().getId().equals(member.getId())) {
            throw GeneralException.of(StatusCode.UNAUTHORIZED_ACCESS);
        }
    }

    private Member getLoginMember(Authentication authentication) {
        return (Member) authentication.getPrincipal();
    }

    private ScheduleResponse toResponse(Schedule schedule) {
        ScheduleResponse response = new ScheduleResponse();
        response.setId(schedule.getId());
        response.setName(schedule.getName());
        response.setStartDate(schedule.getStartDate());
        response.setEndDate(schedule.getEndDate());
        response.setColor(schedule.getColor());
        response.setHealthCheck(schedule.getHealthCheck());
        return response;
    }
}
