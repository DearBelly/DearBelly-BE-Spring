package com.hanium.mom4u.domain.question.repository;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.question.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LetterRepository extends JpaRepository<Letter, Long> {

    @Query("""
        select i
        from Letter i
        where i.family = :family
          and i.createdAt between :start and :end
        order by i.createdAt desc
    """)
    List<Letter> findByFamilyAndCreatedAtBetween(
            Family family, LocalDateTime start, LocalDateTime end);


    @Query("""
      select l from Letter l
      where l.writer = :writer
        and l.createdAt between :start and :end
      order by l.createdAt desc
    """)
    List<Letter> findByWriterAndCreatedAtBetween(
            Member writer, LocalDateTime start, LocalDateTime end);

    @Modifying
    @Query("update Member m set m.hasSeenFamilyLetters = true where m.id = :memberId")
    void markSeenForMember(@Param("memberId") Long memberId);

    @Modifying
    @Query("""
  update Member m
     set m.hasSeenFamilyLetters = false
   where m.family.id = :familyId
     and m.id <> :writerId
""")
    void resetSeenFlagForFamilyExceptWriter(Long familyId, Long writerId);
}
