package com.hanium.mom4u.domain.question.repository;

import com.hanium.mom4u.domain.question.entity.DailyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DailyQuestionRepository
        extends JpaRepository<DailyQuestion, Long>, DailyQuestionRepositoryCustom {
}
