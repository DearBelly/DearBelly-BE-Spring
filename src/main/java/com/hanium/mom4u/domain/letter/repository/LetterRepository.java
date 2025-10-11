package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.letter.entity.Letter;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LetterRepository extends JpaRepository<Letter, Long>, LetterRepositoryCustom {
    boolean existsByWriter_IdAndCreatedAtBetween(Long writerId, LocalDateTime start, LocalDateTime end);
    Optional<Letter> findTopByWriter_IdAndCreatedAtBetweenOrderByCreatedAtDesc(Long writerId, LocalDateTime start, LocalDateTime end);
}
