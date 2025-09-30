package com.hanium.mom4u.domain.family.entity;

import com.hanium.mom4u.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "daily_question")
public class DailyQuestion extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_question_id")
    private Long id;

    @Column(name = "daily_question_text", nullable = false) // ← 그날 배정된 질문 텍스트
    private String questionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(name = "writer")
    private String writer;

    @Column(name = "answer")
    private String answer;

    @Column(name = "origin_question_id", nullable = false) // ← 원본 Question FK
    private Long questionId;
}

