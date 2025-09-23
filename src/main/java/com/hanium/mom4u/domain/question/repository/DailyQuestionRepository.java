package com.hanium.mom4u.domain.question.repository;

import com.hanium.mom4u.domain.family.entity.DailyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DailyQuestionRepository extends JpaRepository<DailyQuestion, Long> {

    @Query("""
      select dq from DailyQuestion dq
      where dq.family.id = :familyId
        and dq.createdAt between :start and :end
      order by dq.createdAt desc
    """)
    List<DailyQuestion> findLatestForFamily(@Param("familyId") Long familyId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            Pageable p);

    @Query("""
      select dq from DailyQuestion dq
      where dq.family is null
        and dq.createdAt between :start and :end
      order by dq.createdAt desc
    """)
    List<DailyQuestion> findLatestGlobal(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         Pageable p);

    boolean existsByFamilyIsNullAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = """
    SELECT q.question_id AS id, q.content AS content
      FROM question q
     WHERE (:excludeContent IS NULL OR q.content <> :excludeContent)
     ORDER BY RAND()
     LIMIT 1
""", nativeQuery = true)
    Optional<QuestionPick> pickRandomQuestionExcludingContent(@Param("excludeContent") String excludeContent);

    // 아무거나 1개 (fallback)
    @Query(value = """
    SELECT q.question_id AS id, q.content AS content
      FROM question q
     ORDER BY RAND()
     LIMIT 1
""", nativeQuery = true)
    Optional<QuestionPick> pickAnyQuestion();

    interface QuestionPick {
        Long getId();
        String getContent();
    }
}
