package com.hanium.mom4u.domain.calendar.service;

import com.hanium.mom4u.domain.calendar.dto.request.ScheduleRequest;
import com.hanium.mom4u.domain.calendar.dto.response.ScheduleResponse;
import com.hanium.mom4u.domain.calendar.entity.Schedule;
import com.hanium.mom4u.domain.calendar.repository.ScheduleRepository;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
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
    private final AuthenticatedProvider authenticatedProvider;

    public List<ScheduleResponse> getSchedulesByMonth(int year, int month) {
        Member member = authenticatedProvider.getCurrentMember();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());

        if (member.getFamily() != null) {
            // 가족 전체 일정 반환
            return scheduleRepository.findAllByMemberFamilyIdAndStartDateBetween(member.getFamily().getId(), start, end)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } else {
            // 개인 일정만
            return scheduleRepository.findAllByMemberIdAndStartDateBetween(member.getId(), start, end)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
    }



    public List<ScheduleResponse> getSchedulesByDate(LocalDate date) {
        Member member = authenticatedProvider.getCurrentMember();

        if (member.getFamily() != null) {
            return scheduleRepository.findAllByMemberFamilyIdAndStartDate(member.getFamily().getId(), date)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } else {
            return scheduleRepository.findAllByMemberAndStartDate(member, date)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
    }


    public ScheduleResponse getScheduleDetail(Long scheduleId) {
        Member member = authenticatedProvider.getCurrentMember();
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> GeneralException.of(StatusCode.SCHEDULE_NOT_FOUND));
        validateOwnership(schedule, member);
        return toResponse(schedule);
    }

    public void createSchedule(ScheduleRequest request) {
        Member member = authenticatedProvider.getCurrentMember();

        // 일별 최대 10개 제한
        long count = scheduleRepository.countByMemberAndStartDate(member, request.getStartDate());
        if (count >= 10) {
            throw GeneralException.of(StatusCode.SCHEDULE_LIMIT_EXCEEDED);
        }

        Schedule schedule = Schedule.builder()
                .schedule(request.getSchedule())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .color(request.getColor())
                .member(member)
                .build();
        scheduleRepository.save(schedule);
    }

    public void updateSchedule(Long scheduleId, ScheduleRequest request) {
        Member member = authenticatedProvider.getCurrentMember();
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> GeneralException.of(StatusCode.SCHEDULE_NOT_FOUND));
        validateOwnership(schedule, member);
        schedule.update(
                request.getSchedule(),
                request.getStartDate(),
                request.getEndDate(),
                request.getColor()
        );
    }

    public void deleteSchedule(Long scheduleId) {
        Member member = authenticatedProvider.getCurrentMember();
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> GeneralException.of(StatusCode.SCHEDULE_NOT_FOUND));
        validateOwnership(schedule, member);
        scheduleRepository.delete(schedule);
    }

    private void validateOwnership(Schedule schedule, Member currentMember) {
        // 본인 일정이면 OK
        if (schedule.getMember().getId().equals(currentMember.getId())) return;

        // 가족이 존재하고, 일정 소유자도 같은 가족이면 OK
        if (currentMember.getFamily() != null &&
                schedule.getMember().getFamily() != null &&
                currentMember.getFamily().getId().equals(schedule.getMember().getFamily().getId())) {
            return;
        }

        // 둘 다 아니면 예외
        throw GeneralException.of(StatusCode.UNAUTHORIZED_ACCESS);
    }


    private ScheduleResponse toResponse(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .schedule(schedule.getSchedule())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .color(schedule.getColor())
                .build();
    }
}

