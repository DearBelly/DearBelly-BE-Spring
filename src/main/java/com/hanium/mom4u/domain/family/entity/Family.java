package com.hanium.mom4u.domain.family.entity;

import com.hanium.mom4u.domain.common.BaseEntity;
import com.hanium.mom4u.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
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

    @OneToMany(mappedBy = "family", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> memberList = new ArrayList<>();


    @OneToMany(mappedBy = "family", fetch = FetchType.LAZY)
    private List<DailyQuestion> dailyQuestionList = new ArrayList<>();

    public void addMember(Member member) {
        if (member == null) return;
        if (!memberList.contains(member)) memberList.add(member);
        if (member.getFamily() != this) member.setFamily(this);
    }
}
