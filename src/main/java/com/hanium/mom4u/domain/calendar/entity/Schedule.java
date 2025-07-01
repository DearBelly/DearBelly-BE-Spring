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

    @Column(name = "name")
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "color")
    private Color color;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "health_check")
    private String healthCheck;


    // Schedule.java (entity)
    public void update(String name, LocalDate startDate, LocalDate endDate, Color color, String healthCheck) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.color = color;
        this.healthCheck = healthCheck;
    }

}
