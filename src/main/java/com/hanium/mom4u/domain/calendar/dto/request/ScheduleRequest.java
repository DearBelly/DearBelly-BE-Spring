package com.hanium.mom4u.domain.calendar.dto.request;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hanium.mom4u.domain.calendar.common.Color;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class ScheduleRequest {
    private String schedule;
    private LocalDate startDate;
    private LocalDate endDate;
    private Color color;


}
