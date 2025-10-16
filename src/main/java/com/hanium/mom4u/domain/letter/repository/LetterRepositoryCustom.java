package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.letter.entity.Letter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LetterRepositoryCustom {

    // 오늘날짜에 대하여 내가 작성한 게 있는지 확인
    Optional<Letter> findTodayByWriterId(Long memberId, LocalDate today);

    // 오늘날짜에 대하여 읽지 않은 편지 반환
    boolean findExistsByMemberId(Long memberId, LocalDate date);

    // 공용 범위 조회: 내 글/가족 글/커서/페이지 모두 커버
    List<Letter> findLetters(
            Long meId,
            Long familyId,
            LocalDateTime start,
            LocalDateTime end,
            LocalDateTime cursor,   // (createdAt < cursor)
            org.springframework.data.domain.Pageable pageable,
            boolean fetchWriter     // fetch join 옵션
    );
}
