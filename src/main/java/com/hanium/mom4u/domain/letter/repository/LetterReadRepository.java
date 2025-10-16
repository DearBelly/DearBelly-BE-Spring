package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.letter.entity.LetterRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface LetterReadRepository extends JpaRepository<LetterRead, Long> {


    /** 회원 기준으로 아직 읽지 않은 편지가 존재하는지 (날짜 무관, readAt is null) */

    boolean existsByMember_IdAndReadAtIsNull(Long memberId);

    List<LetterRead> findByMember_IdAndLetter_IdIn(Long memberId, Collection<Long> letterIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update LetterRead lr set lr.readAt = :ts " +
            "where lr.letter.id = :letterId and lr.member.id = :memberId and lr.readAt is null")
    int markRead(@Param("letterId") Long letterId,
                 @Param("memberId") Long memberId,
                 @Param("ts")LocalDateTime ts);
}
