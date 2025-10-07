package com.hanium.mom4u.domain.letter.repository;

import java.time.LocalDate;

public interface LetterRepositoryCustom {

    // 오늘날짜에 대하여 읽지 않은 편지 반환
    boolean findExistsByMemberId(Long receiverId, LocalDate date);
}
