package com.hanium.mom4u.domain.question.repository;

import com.hanium.mom4u.domain.question.dto.response.LetterCheckResponseDto;
import com.hanium.mom4u.domain.question.entity.Letter;

import java.time.LocalDate;
import java.util.Optional;

public interface LetterRepositoryCustom {

    // 오늘날짜에 대하여 읽지 않은 편지 반환
    Optional<LetterCheckResponseDto> findOneByReceiverId(Long receiverId, LocalDate date);
}
