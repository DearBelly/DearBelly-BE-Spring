package com.hanium.mom4u.domain.calendar.controller;

import com.hanium.mom4u.domain.calendar.dto.request.ScheduleRequest;
import com.hanium.mom4u.domain.calendar.dto.response.ScheduleResponse;
import com.hanium.mom4u.domain.calendar.service.ScheduleService;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "일정 API", description = "개인 일정 등록, 조회, 수정, 삭제 API")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "월별 일정 조회", description = "사용자의 특정 연도/월에 해당하는 모든 일정을 조회합니다.")
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlySchedules(
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByMonth(year, month);
        return ResponseEntity.ok(CommonResponse.onSuccess(schedules));
    }

    @Operation(summary = "일별 일정 조회", description = "사용자의 특정 날짜에 해당하는 모든 일정을 조회합니다.")
    @GetMapping("/daily")
    public ResponseEntity<?> getDailySchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDate(date);
        return ResponseEntity.ok(CommonResponse.onSuccess(schedules));
    }

    @Operation(summary = "일정 등록", description = "사용자의 새 일정을 생성합니다.")
    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody ScheduleRequest request) {
        scheduleService.createSchedule(request);
        return ResponseEntity.ok(CommonResponse.onSuccess());
    }

    @Operation(summary = "일정 수정", description = "지정한 ID의 일정을 수정합니다.")
    @PatchMapping("/{scheduleId}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long scheduleId, @RequestBody ScheduleRequest request) {
        scheduleService.updateSchedule(scheduleId, request);
        return ResponseEntity.ok(CommonResponse.onSuccess());
    }

    @Operation(summary = "일정 삭제", description = "지정한 ID의 일정을 삭제합니다.")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(CommonResponse.onSuccess());
    }

    @Operation(summary = "일정 상세 조회", description = "지정한 ID의 일정 정보를 상세 조회합니다.")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<?> getScheduleDetail(@PathVariable Long scheduleId) {
        ScheduleResponse response = scheduleService.getScheduleDetail(scheduleId);
        return ResponseEntity.ok(CommonResponse.onSuccess(response));
    }
}
