package com.hanium.mom4u.domain.question.repository;

import com.hanium.mom4u.domain.question.entity.LetterRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LetterReadRepository extends JpaRepository<LetterRead, Long> {


    /** 남이 쓴 것 중 내가 안 읽은 편지 개수 (가족 단위) */
    @Query("""
        SELECT COUNT(l) FROM Letter l
        WHERE l.family.id = :familyId
          AND l.writer.id <> :memberId
          AND NOT EXISTS (
              SELECT 1 FROM LetterRead r
              WHERE r.letter = l AND r.reader.id = :memberId
          )
    """)
    long countUnreadForMember(@Param("familyId") Long familyId,
                              @Param("memberId") Long memberId);
}
