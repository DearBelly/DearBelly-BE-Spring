package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.letter.entity.Letter;
import com.hanium.mom4u.domain.letter.entity.QLetterRead;
import com.hanium.mom4u.domain.letter.entity.QLetter;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LetterRepositoryCustomImpl implements LetterRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QLetter letter = QLetter.letter;
    private final QLetterRead letterRead = QLetterRead.letterRead;

    private static LocalDateTime startOfDay(LocalDate d) {
        return d.atStartOfDay();
    }
    private static LocalDateTime nextStartOfDay(LocalDate d) {
        return d.plusDays(1).atStartOfDay();
    }

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

    @Override
    public List<Letter> findLetters(
            Long meId,
            Long familyId,
            LocalDateTime start,
            LocalDateTime end,
            LocalDateTime cursor,
            Pageable pageable,
            boolean fetchWriter
    ) {
        var query = jpaQueryFactory
                .selectFrom(letter);

        if (fetchWriter) {
            query.leftJoin(letter.writer).fetchJoin();
        }

        var predicates = new ArrayList<Predicate>();

        // 범위
        if (start != null) predicates.add(letter.createdAt.goe(start));
        if (end   != null) predicates.add(letter.createdAt.lt(end));

        // 커서
        if (cursor != null) predicates.add(letter.createdAt.lt(cursor));

        // familyId 없으면 내 글 + family null 글
        if (familyId == null) {
            predicates.add(
                    letter.writer.id.eq(meId)
                            .or(letter.writer.id.eq(meId).and(letter.family.isNull()))
            );
        } else {
            predicates.add(letter.family.id.eq(familyId));
        }

        query.where(predicates.toArray(com.querydsl.core.types.Predicate[]::new))
                .orderBy(letter.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return query.fetch();
    }
}
