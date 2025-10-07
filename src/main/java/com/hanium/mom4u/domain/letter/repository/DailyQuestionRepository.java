package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.family.entity.DailyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DailyQuestionRepository
        extends JpaRepository<DailyQuestion, Long>, DailyQuestionRepositoryCustom {
}
