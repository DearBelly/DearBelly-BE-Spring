package com.hanium.mom4u.domain.calendar.dto.response;

import com.hanium.mom4u.domain.calendar.common.Color;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Builder
public class ScheduleResponse {
    private Long id;
    private String schedule;
    private LocalDate startDate;
    private LocalDate endDate;
    private Color color;
}
