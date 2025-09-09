package com.hanium.mom4u.domain.family.entity;

import com.hanium.mom4u.domain.common.BaseEntity;
import com.hanium.mom4u.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "family")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Family extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "lmp_date")
    private LocalDate lmpDate;

    @OneToMany(mappedBy = "family")
    private List<Member> memberList;

    @OneToMany(mappedBy = "family")
    private List<DailyQuestion> dailyQuestionList;

}