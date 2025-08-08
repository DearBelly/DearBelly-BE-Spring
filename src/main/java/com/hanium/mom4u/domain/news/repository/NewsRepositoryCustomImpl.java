package com.hanium.mom4u.domain.news.repository;

import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.entity.News;
import com.hanium.mom4u.domain.news.entity.QNews;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NewsRepositoryCustomImpl implements NewsRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QNews news = QNews.news;

    /**
     * 각 카테고리 별로 하나씩 반환
     * 반환은 post_id 컬럼을 기준으로
     */
    @Override
    public List<News> findLatestIdByCategory() {
        return jpaQueryFactory
                .selectFrom(news)
                .where(news.id.in(
                        JPAExpressions
                                .select(news.id.max())
                                .from(news)
                                .groupBy(news.category)
                ))
                .fetch();
    }

    @Override
    public List<News> findByCategoryOrderByPostedAt(Category category, int count) {

        return jpaQueryFactory
                .selectFrom(news)
                .where(news.category.eq(category))
                .orderBy(news.postedAt.desc())
                .limit(count)
                .fetch();
    }
}
