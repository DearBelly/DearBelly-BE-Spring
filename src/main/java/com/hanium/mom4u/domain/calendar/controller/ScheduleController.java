package com.hanium.mom4u.domain.calendar.controller;

import com.hanium.mom4u.domain.calendar.dto.request.ScheduleRequest;
import com.hanium.mom4u.domain.calendar.dto.response.ScheduleResponse;
import com.hanium.mom4u.domain.calendar.service.ScheduleService;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final MemberRepository memberRepository;

    @GetMapping
    public List<ScheduleResponse> getAllSchedules() {
        return scheduleService.getAllSchedules();
    }

    @PostMapping
    public ResponseEntity<Void> createSchedule(
            @RequestParam Long memberId,
            @RequestBody ScheduleRequest request
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        scheduleService.createSchedule(request, member);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{scheduleId}")
    public ResponseEntity<Void> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleRequest request
    ) {
        scheduleService.updateSchedule(scheduleId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{scheduleId}")
    public ScheduleResponse getScheduleDetail(@PathVariable Long scheduleId) {
        return scheduleService.getScheduleDetail(scheduleId);
    }
}
