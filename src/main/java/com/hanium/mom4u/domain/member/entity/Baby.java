package com.hanium.mom4u.domain.member.entity;

import com.hanium.mom4u.domain.common.BaseEntity;
import com.hanium.mom4u.domain.member.common.BabyGender;
import com.hanium.mom4u.domain.member.dto.request.BabyInfoRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "baby")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Baby extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "baby_id")
    private Long id;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "pregnant_date")
    private LocalDate pregnantDate;

    @Column(name = "week_level")
    private int weekLevel;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "is_ended")
    private boolean isEnded;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private BabyGender gender;

    @Column(name = "name")
    private String name;

    @Column(name = "birth_year")
    private int birthYear;

    @ManyToOne
    @Setter
    @JoinColumn(name = "member_id")
    private Member member;

    public Baby updateInfo(BabyInfoRequestDto requestDto) {
        if (requestDto.getName() != null && !requestDto.getName().isEmpty()) {
            this.name = requestDto.getName();
        }
        if (requestDto.getPregnantDate() != null) {
            this.pregnantDate = requestDto.getPregnantDate();
        }
        if (requestDto.getBabyGender() != null) {
            this.gender = requestDto.getBabyGender();
        }

        if (requestDto.getWeekLevel() > 0) {
            this.weekLevel = requestDto.getWeekLevel();
        }

        return this;
    }
}
