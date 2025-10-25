package com.hanium.mom4u.domain.member.entity;

import com.hanium.mom4u.domain.common.BaseEntity;
import com.hanium.mom4u.domain.member.common.Gender;
import com.hanium.mom4u.domain.member.dto.request.BabyInfoRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

    /** 마지막 생리 시작일 (LMP) */
    @Column(name = "lmp_date")
    private LocalDate lmpDate;

    @Column(name = "is_ended")
    private boolean isEnded;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender babyGender;

    @Column(name = "name")
    private String name;

    @Column(name = "birth_year")
    private int birthYear;

    @ManyToOne
    @Setter
    @JoinColumn(name = "member_id")
    private Member member;

    public Baby updateInfo(BabyInfoRequestDto dto) {
        if (dto.getName() != null && !dto.getName().isBlank()) this.name = dto.getName();
        if (dto.getBabyGender() != null) this.babyGender = dto.getBabyGender();
        return this;
    }

    /** dueDate 기준으로 0주차부터 계산 */
    public int getCurrentWeek() {
        if (lmpDate == null) return 0;
        long days = ChronoUnit.DAYS.between(lmpDate, LocalDate.now());
        return (int) Math.max(0, days / 7);
    }

    @Transient
    public LocalDate getDueDateCalculated() {
        return (lmpDate == null) ? null : lmpDate.plusWeeks(40);
    }

    public void setLmpDate(LocalDate lmpDate) {
        this.lmpDate = lmpDate;
    }

    public Baby(Long id, boolean isEnded, Gender babyGender, String name, int birthYear, Member member) {
        this.id = id;
        this.isEnded = isEnded;
        this.babyGender = babyGender;
        this.name = name;
        this.birthYear = birthYear;
        this.member = member;
    }
}
