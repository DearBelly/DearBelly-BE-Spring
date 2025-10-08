package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.letter.entity.Letter;
import com.hanium.mom4u.domain.letter.entity.QLetterRead;
import com.hanium.mom4u.domain.letter.entity.QLetter;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LetterRepositoryCustomImpl implements LetterRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QLetter letter = QLetter.letter;

    /*
    오늘 일자를 기준으로 안 읽은 편지가 있는지 조회
     */
    @Override
    public boolean findExistsByMemberId(Long memberId, LocalDate date) {

        LocalDateTime start = date.atStartOfDay(); // LocalDate -> LocalDateTime
        LocalDateTime end = date.plusDays(1).atStartOfDay(); // 다음 날의 시작 시각

        QLetterRead letterRead = QLetterRead.letterRead;
        Integer exists = jpaQueryFactory
                .selectOne()
                .from(letter)
                .join(letterRead).on(
                        letterRead.letter.eq(letter)
                )
                .where(
                        letterRead.member.id.eq(memberId),
                        letter.createdAt.goe(start),
                        letter.createdAt.lt(end),
                        letterRead.readAt.isNull()
                )
                .fetchFirst();

        return exists != null;
    }

    /*
    작성자가 자기 자신이고, 오늘자 날짜로 작성 있는지 확인
     */
    @Override
    public Optional<Letter> findTodayByWriterId(Long memberId, LocalDate today) {

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end   = start.plusDays(1);

        Letter result = jpaQueryFactory
                .selectFrom(letter)
                .where(
                        letter.writer.id.eq(memberId),
                        letter.createdAt.goe(start),
                        letter.createdAt.lt(end)
                )
                .orderBy(letter.createdAt.desc())
                .fetchOne();
        return Optional.ofNullable(result);
    }
}
