package com.hanium.mom4u.domain.calendar.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter

public class ScheduleRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String color; // Enum 이름 (예: "RED")
    private String healthCheck;
}
