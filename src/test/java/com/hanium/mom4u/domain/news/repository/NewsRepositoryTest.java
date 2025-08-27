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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDSLConfig.class)
public class NewsRepositoryTest {

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

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
    @DisplayName("카테고리별 슬라이스 조회 및 페이지네이션 성공")
    void 카테고리별_슬라이스조회_테스트() {
        // given
        // HEALTH 3건, FINANCIAL 2건
        LocalDate today = LocalDate.now();
        saveNews(101L, Category.HEALTH, "H1", today);
        saveNews(102L, Category.HEALTH, "H2", today);
        saveNews(103L, Category.HEALTH, "H3", today);
        saveNews(201L, Category.FINANCIAL, "F1", today);
        saveNews(202L, Category.FINANCIAL, "F2", today);

        // when
        Pageable firstPage = PageRequest.of(0, 2);
        Slice<News> pSlice1 = newsRepository.findByCategory(Category.HEALTH, firstPage);
        Pageable secondPage = PageRequest.of(1, 2);
        Slice<News> pSlice2 = newsRepository.findByCategory(Category.HEALTH, secondPage);

        // then
        assertThat(pSlice1.getContent()).hasSize(2);
        assertThat(pSlice2.getContent()).hasSize(1);

        // 전부 HEALTH인지를 확인하기
        assertThat(pSlice1.getContent()).allMatch(n -> n.getCategory() == Category.HEALTH);
        assertThat(pSlice2.getContent()).allMatch(n -> n.getCategory() == Category.HEALTH);
    }

    @Test
    @DisplayName("postId 내림차순으로 슬라이스 조회 성공")
    void 내림차순으로_슬라이스_조회_테스트() {
        // given (postId 뒤로 갈수록 큼)
        LocalDate today = LocalDate.now();
        saveNews(10L, Category.HEALTH, "H10", today);
        saveNews(30L, Category.FINANCIAL, "F30", today);
        saveNews(20L, Category.CHILD, "C20", today);
        saveNews(40L, Category.EMOTIONAL, "E40", today);

        // when
        Slice<News> slice = newsRepository.findAllOrderByPostId(PageRequest.of(0, 3));

        // then
        List<Long> postIds = slice.getContent().stream().map(News::getPostId).toList();
        assertThat(postIds).containsExactly(40L, 30L, 20L);
        assertThat(slice.hasNext()).isTrue();
    }

    @Test
    @DisplayName("페이징 두 번째 페이지 검증 성공")
    void 페이징_두번째페이지_테스트() {
        // given
        LocalDate today = LocalDate.now();
        for (long i = 1; i <= 6; i++) {
            saveNews(i, Category.EMOTIONAL, "E" + i, today);
        }

        // when
        Slice<News> page0 = newsRepository.findAllOrderByPostId(PageRequest.of(0, 2));
        Slice<News> page1 = newsRepository.findAllOrderByPostId(PageRequest.of(1, 2));
        Slice<News> page2 = newsRepository.findAllOrderByPostId(PageRequest.of(2, 2));

        // then
        assertThat(page0.getContent().stream().map(News::getPostId).toList())
                .containsExactly(6L, 5L);
        assertThat(page1.getContent().stream().map(News::getPostId).toList())
                .containsExactly(4L, 3L);
        assertThat(page2.getContent().stream().map(News::getPostId).toList())
                .containsExactly(2L, 1L);

        assertThat(page0.hasNext()).isTrue();
        assertThat(page1.hasNext()).isTrue();
        assertThat(page2.hasNext()).isFalse();
    }
}
