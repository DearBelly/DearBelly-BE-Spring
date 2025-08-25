package com.hanium.mom4u.domain.news.repository;

import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.entity.News;
import com.hanium.mom4u.global.config.QueryDSLConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDSLConfig.class)
class NewsRepositoryCustomTest {

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("JPAQueryFactory 로드 테스트")
    void jpaQueryFactory_is_loaded() {
        assertThat(jpaQueryFactory).isNotNull();
    }

    @Autowired
    private NewsRepository newsRepository;

    private News saveNews(Long postId, Category category, String title, LocalDate postedAt) {
        News news = new News();
        news.setPostId(postId);
        news.setCategory(category);
        news.setTitle(title);
        news.setPostedAt(postedAt);
        return newsRepository.save(news);
    }

    @Test
    @DisplayName("카테고리별 가장 최신 뉴스 1건씩 반환 성공")
    void 카테고리별_가장_최신_뉴스_1건씩_반환() {
        // given
        saveNews(1L, Category.HEALTH, "H-OLD", LocalDate.now().minusDays(2));
        saveNews(2L, Category.HEALTH, "H-NEW", LocalDate.now());
        saveNews(3L, Category.FINANCIAL, "F-NEW", LocalDate.now());
        saveNews(4L, Category.EMOTIONAL, "E-NEW", LocalDate.now());

        em.flush();
        em.clear();

        // when
        List<News> latestList = newsRepository.findLatestIdByCategory();

        // then
        // 결과를 카테고리 -> 뉴스맵으로 변환
        Map<Category, News> map = latestList.stream()
                .collect(Collectors.toMap(News::getCategory, n -> n));

        assertThat(map).containsKeys(Category.HEALTH, Category.FINANCIAL, Category.EMOTIONAL);
        assertThat(map.get(Category.HEALTH).getTitle()).isEqualTo("H-NEW");
        assertThat(map.get(Category.FINANCIAL).getTitle()).isEqualTo("F-NEW");
        assertThat(map.get(Category.EMOTIONAL).getTitle()).isEqualTo("E-NEW");
    }

    @Test
    @DisplayName("지정 카테고리 최신 N건 반환 테스트")
    void 지정_카테고리_최신_N건반환_테스트() {
        // given
        saveNews(11L, Category.CHILD, "C-OLD", LocalDate.now().minusDays(3));
        saveNews(12L, Category.CHILD, "C-MID", LocalDate.now().minusDays(2));
        saveNews(13L, Category.CHILD, "C-NEW", LocalDate.now().minusDays(1));

        em.flush();
        em.clear();

        // when
        List<News> top2 = newsRepository.findByCategoryOrderByPostedAt(Category.CHILD, 2);

        // then
        assertThat(top2).hasSize(2);
        // 2개 반환 확인하고 내림차순
        assertThat(top2.get(0).getTitle()).isEqualTo("C-NEW");
        assertThat(top2.get(1).getTitle()).isEqualTo("C-MID");
    }

}