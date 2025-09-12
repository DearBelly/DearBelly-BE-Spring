package com.hanium.mom4u.domain.calendar.entity;

import com.hanium.mom4u.domain.calendar.common.Color;
import com.hanium.mom4u.domain.common.BaseEntity;
import com.hanium.mom4u.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "calendar")
public class Schedule extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_id")
    private Long id;

    @Column(name = "schedule")
    private String schedule;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", length = 16, nullable = false)
    private Color color;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;


    // Schedule.java (entity)
    public void update(String schedule, LocalDate startDate, LocalDate endDate, Color color) {
        this.schedule = schedule;
        this.startDate = startDate;
        this.endDate = endDate;
        this.color = color;
    }

}
