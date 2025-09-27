package com.hanium.mom4u.domain.question.repository;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.question.entity.Letter;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LetterRepository extends JpaRepository<Letter, Long>, LetterRepositoryCustom {

    @Query("""
        select l from Letter l
        join fetch l.writer w
        where l.family = :family
          and l.createdAt between :start and :end
        order by l.createdAt desc
    """)
    List<Letter> findByFamilyAndCreatedAtBetween(
            Family family, LocalDateTime start, LocalDateTime end);

    @Query("""
      select l from Letter l
      join fetch l.writer w
      where l.writer = :writer
        and l.createdAt between :start and :end
      order by l.createdAt desc
    """)
    List<Letter> findByWriterAndCreatedAtBetween(
            Member writer, LocalDateTime start, LocalDateTime end);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Member m set m.hasSeenFamilyLetters = true where m.id = :memberId")
    void markSeenForMember(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Member m
           set m.hasSeenFamilyLetters = false
         where m.family.id = :familyId
           and m.id <> :writerId
    """)
    void resetSeenFlagForFamilyExceptWriter(@Param("familyId") Long familyId,
                                            @Param("writerId") Long writerId);

    boolean existsByWriter_IdAndCreatedAtBetween(Long writerId, LocalDateTime start, LocalDateTime end);

    Optional<Letter> findTopByWriter_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long writerId, LocalDateTime start, LocalDateTime end);



    @Query("""
    select l from Letter l
    join fetch l.writer w
    where
    (
      (:familyId is null and l.writer.id = :meId)
      or (:familyId is not null and l.family.id = :familyId)
      or (l.writer.id = :meId and l.family is null)
    )
    and (:cursor is null or l.createdAt < :cursor)
    order by l.createdAt desc
    """)
    List<Letter> findFeedForUser(@Param("meId") Long meId,
                                 @Param("familyId") Long familyId,
                                 @Param("cursor") LocalDateTime cursor,
                                 org.springframework.data.domain.Pageable pageable);
}
