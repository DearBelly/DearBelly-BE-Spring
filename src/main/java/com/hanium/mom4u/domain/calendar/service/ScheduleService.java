package com.hanium.mom4u.domain.calendar.service;

import com.hanium.mom4u.domain.calendar.common.Color;
import com.hanium.mom4u.domain.calendar.dto.request.ScheduleRequest;
import com.hanium.mom4u.domain.calendar.dto.response.ScheduleResponse;
import com.hanium.mom4u.domain.calendar.entity.Schedule;
import com.hanium.mom4u.domain.calendar.repository.ScheduleRepository;
import com.hanium.mom4u.domain.member.entity.Member;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    // 전체 일정 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 일정 저장
    public void createSchedule(ScheduleRequest request, Member member) {
        Schedule schedule = Schedule.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .color(Color.valueOf(request.getColor()))
                .healthCheck(request.getHealthCheck())
                .member(member)
                .build();

        scheduleRepository.save(schedule);
    }

    // 일정 수정 (scheduleId를 받아서 수정하도록 고침!)
    public void updateSchedule(Long scheduleId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        schedule.update(
                request.getName(),
                request.getStartDate(),
                request.getEndDate(),
                Color.valueOf(request.getColor()),
                request.getHealthCheck()
        );
    }

    // 일정 삭제
    public void deleteSchedule(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    // 일정 상세 조회
    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleDetail(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        return toResponse(schedule);
    }

    // 엔티티 → 응답 DTO 변환
    private ScheduleResponse toResponse(Schedule schedule) {
        ScheduleResponse response = new ScheduleResponse();
        response.setId(schedule.getId());
        response.setName(schedule.getName());
        response.setStartDate(schedule.getStartDate());
        response.setEndDate(schedule.getEndDate());
        response.setColor(schedule.getColor().name());
        response.setHealthCheck(schedule.getHealthCheck());
        return response;
    }
}
