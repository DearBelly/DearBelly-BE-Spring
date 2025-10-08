package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.letter.entity.Letter;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LetterRepository extends JpaRepository<Letter, Long>, LetterRepositoryCustom {

    /*
    해당 날짜를 기준으로 한 모든 편지 읽기 및 읽음 처리
     */
    @Query("""
        select l from Letter l
            where l.family.id = :familyId
            and l.createdAt between :start and :end
        order by l.createdAt desc
    """)
    List<Letter> findLetterByYearAndMonth(
            @Param("familyId") Long familyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);


    /*
    작성 날짜와 fmailyId로 Letter List 조회
     */
    @Query("""
        select l from Letter l
        join fetch l.writer w
        where l.family = :family
          and l.createdAt between :start and :end
        order by l.createdAt desc
    """)
    List<Letter> findByFamilyAndCreatedAtBetween(
            Family family, LocalDateTime start, LocalDateTime end);


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
