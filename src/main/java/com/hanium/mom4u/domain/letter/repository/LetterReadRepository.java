package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.letter.entity.LetterRead;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LetterReadRepository extends JpaRepository<LetterRead, Long> {

    // 작성 편지의 ID와 회원 ID를 통하여 조회
    Optional<LetterRead> findByLetterIdAndMemberId(Long letterId, Long memberId);

    /*
    날짜 상관없이 아직 안 읽은 것이 있는지 반환
     */
    @Query("""
    select exists (
        select 1
            from LetterRead lr
            where lr.member.id = :memberId
                and lr.readAt is null
        )
    """)
    boolean existsByLetterIdAndMemberId(@Param("memberId") Long memberId);
}
