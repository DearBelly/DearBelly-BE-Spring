package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.letter.entity.LetterRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LetterReadRepository extends JpaRepository<LetterRead, Long> {

    /** 특정 편지/회원의 읽음 엔티티 조회 */
    Optional<LetterRead> findByLetter_IdAndMember_Id(
            @Param("letterId") Long letterId, @Param("memberId") Long memberId);

    /** 편지 단건에 대해 '읽었는지' 여부 (readAt not null) */
    @Query("""
        select (count(lr) > 0)
        from LetterRead lr
        where lr.letter.id = :letterId
          and lr.member.id = :memberId
          and lr.readAt is not null
    """)
    boolean findExistByLetterIdAndMemberId(
            @Param("letterId") Long letterId, @Param("memberId") Long memberId);

    /** 회원 기준으로 아직 읽지 않은 편지가 존재하는지 (날짜 무관, readAt is null) */
    @Query("""
        select (count(lr) > 0)
        from LetterRead lr
        where lr.member.id = :memberId
          and lr.readAt is null
    """)
    boolean existsUnreadByMemberId(@Param("memberId") Long memberId);
}
