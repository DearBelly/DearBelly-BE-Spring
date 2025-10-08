package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.family.entity.DailyQuestion;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyQuestionRepositoryCustom {

    // 특정 날짜, 가족 질문 우선 조회 (없으면 전역)
    Optional<DailyQuestion> findOneForDate(Long familyId, LocalDate date);

    // 특정 날짜, 전역 질문 조회
    Optional<DailyQuestion> findOneGlobalOn(LocalDate date);

    // 전역 질문 존재 여부
    boolean existsGlobalOn(LocalDate date);

    // 전일 중복 제외하고 랜덤 질문 뽑기
    Optional<QuestionPick> pickRandomExcluding(String excludeContent);

    // 아무 질문 1개 랜덤
    Optional<QuestionPick> pickAny();

    // Projection 인터페이스
    interface QuestionPick {
        Long getId();
        String getContent();
    }
}
