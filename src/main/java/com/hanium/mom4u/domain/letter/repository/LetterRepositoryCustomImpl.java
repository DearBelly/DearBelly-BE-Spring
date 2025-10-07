package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.letter.entity.QLetterRead;
import com.hanium.mom4u.domain.letter.entity.QLetter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class LetterRepositoryCustomImpl implements LetterRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QLetter letter = QLetter.letter;

    /*
    오늘 일자를 기준으로 안 읽은 편지가 있는지 조회
     */
    public boolean findExistsByMemberId(Long memberId, LocalDate date) {

        LocalDateTime start = date.atStartOfDay(); // LocalDate -> LocalDateTime
        LocalDateTime end = date.plusDays(1).atStartOfDay(); // 다음 날의 시작 시각

        // 날짜에 대한 BooleanBuilder
        BooleanBuilder dateBuilder = new BooleanBuilder()
                .and(letter.createdAt.goe(start)) // 이상
                .and(letter.createdAt.lt(end));  // 이하


        QLetterRead letterRead = QLetterRead.letterRead;

        // 오늘날짜에 대하여 없을 수도 있음
        return jpaQueryFactory
                        .selectOne()
                        .from(letter)
                        .join(letterRead).on(
                                letterRead.letter.eq(letter)
                )
                .where(dateBuilder)
                .where(letterRead.readAt.isNull())
                .fetchFirst() != null; // null인지 아닌지 최종적으로 boolean
    }
}
