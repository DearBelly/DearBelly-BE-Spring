package com.hanium.mom4u.domain.calendar.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hanium.mom4u.domain.calendar.common.Color;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class ScheduleRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Color color;
    private String healthCheck;


}
