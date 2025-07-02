package com.hanium.mom4u.domain.family.entity;

import com.hanium.mom4u.domain.common.BaseEntity;
import com.hanium.mom4u.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "family")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Family extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_id")
    private Long id;

    @Column(name = "code")
    private String code;

    @OneToMany(mappedBy = "family")
    private List<Member> memberList;

    @OneToMany(mappedBy = "family")
    private List<DailyQuestion> dailyQuestionList;
}
