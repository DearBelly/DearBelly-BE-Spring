package com.hanium.mom4u.domain.question.entity;

import com.hanium.mom4u.domain.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "letter")
public class LetterRead extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
