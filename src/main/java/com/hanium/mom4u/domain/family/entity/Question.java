package com.hanium.mom4u.domain.family.entity;

import com.hanium.mom4u.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "question")
public class Question extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

}
