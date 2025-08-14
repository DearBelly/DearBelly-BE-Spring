package com.hanium.mom4u.domain.question.service;

import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.question.entity.Letter;
import com.hanium.mom4u.domain.question.entity.LetterRead;
import com.hanium.mom4u.domain.question.repository.LetterReadRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
public class LetterReadService {
    private final LetterReadRepository letterReadRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsRead(Letter letter, Member me) {
        try {
            letterReadRepository.save(LetterRead.of(letter, me));
        } catch (org.springframework.dao.DataIntegrityViolationException ignore) {
            // 이미 읽음 → 무시 (이 트랜잭션만 롤백)
        }
    }
}

