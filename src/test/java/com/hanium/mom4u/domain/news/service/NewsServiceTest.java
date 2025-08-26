package com.hanium.mom4u.domain.news.service;

import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.dto.response.NewsPreviewResponseDto;
import com.hanium.mom4u.domain.news.entity.News;
import com.hanium.mom4u.domain.news.repository.NewsRepository;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@Transactional
@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private AuthenticatedProvider authenticatedProvider;
    @InjectMocks
    private NewsService newsService;

    @AfterEach
    void setDown() {
        SecurityContextHolder.clearContext();
    }

    private Member memberWithNone() {
        Member m = Member.builder()
                .id(1L)
                .name("test")
                .role(Role.ROLE_USER)
                .build();

        return m;
    }

    private Member saveMemberWithInterests(Category... categories) {
        LinkedHashSet<Category> interests = new LinkedHashSet<>();
        for (Category c : categories) {
            interests.add(c);
        }
        Member m = Member.builder()
                .id(1L)
                .name("test")
                .role(Role.ROLE_USER)
                .build();
        m.setInterests(interests);
        return m;
    }

    private News createNews(Category category, String title, LocalDate postedAt) {
        News news = new News();
        news.setCategory(category);
        news.setTitle(title);
        news.setPostedAt(postedAt);
        return news;
    }

    @Nested
    class GetRecommend {

        @Test
        @DisplayName("관심사가 비어있을 때의 테스트")
        void 관심사가_비어있을_때의_테스트() {
            // given (Mock이니까 저장 안되므로 메모리 객체 필요)
            List<News> allNews = new ArrayList<>();
            for (Category c : Category.values()) {
                allNews.add(createNews(c, c.name() + "-1", LocalDate.now().minusDays(2)));
                allNews.add(createNews(c, c.name() + "-2", LocalDate.now().minusDays(1)));
            }
            Member member = memberWithNone();
            when(authenticatedProvider.getCurrentMember()).thenReturn(member);

            // when
            when(newsRepository.findByCategoryOrderByPostedAt(any(Category.class), anyInt()))
                    .thenAnswer(inv -> {
                        Category category = inv.getArgument(0);
                        int count = inv.getArgument(1);
                        return allNews.stream()
                                .filter(n -> n.getCategory() == category)
                                .sorted(Comparator.comparing(News::getPostedAt).reversed())
                                .limit(count)
                                .toList();
                    });

            // when
            List<NewsPreviewResponseDto> result = newsService.getRecommend();

            assertThat(result).hasSize(Math.min(3, Category.values().length));
            // 각 DTO가 실제 저장된 뉴스의 타이틀을 가지고 오는지 간단 확인
            assertThat(result).allMatch(dto -> dto.getTitle() != null);
        }

        @Test
        @DisplayName("관심사가 하나일 때의 테스트")
        void 관심사가_하나일_때의_테스트() {
            // given
            News n1 = createNews(Category.EMOTIONAL, "E-1", LocalDate.now().minusDays(3));
            News n2 = createNews(Category.EMOTIONAL, "E-2", LocalDate.now().minusDays(2));
            News n3 = createNews(Category.EMOTIONAL, "E-3", LocalDate.now().minusDays(1));
            List<News> allNews = List.of(n1, n2, n3);

            Member member = saveMemberWithInterests(Category.EMOTIONAL);
            when(authenticatedProvider.getCurrentMember()).thenReturn(member);


            when(newsRepository.findByCategoryOrderByPostedAt(any(Category.class), anyInt()))
                    .thenAnswer(inv -> {
                        Category category = inv.getArgument(0);
                        int count = inv.getArgument(1);
                        return allNews.stream()
                                .filter(n -> n.getCategory() == category)
                                .sorted(Comparator.comparing(News::getPostedAt).reversed())
                                .limit(count)
                                .toList();
                    });

            // when
            var result = newsService.getRecommend();

            assertThat(result).hasSize(3);
            assertThat(result).extracting(NewsPreviewResponseDto::getTitle)
                    .containsExactlyInAnyOrder("E-1","E-2","E-3");
        }

        @Test
        @DisplayName("관심사가 2개일 때의 테스트")
        void 관심사가_2개일_때의_테스트() {
            // 어떤 카테고리에 2개/1개가 갈지는 shuffle에 의존하므로 개수만 검증
            // given
            News news1 = createNews(Category.CHILD, "C-1", LocalDate.now().minusDays(2));
            News news2 = createNews(Category.CHILD, "C-2", LocalDate.now().minusDays(1));
            News news3 = createNews(Category.HEALTH, "H-1", LocalDate.now().minusDays(1));
            List<News> allNews = List.of(news1, news2, news3);

            Member member = saveMemberWithInterests(Category.HEALTH, Category.CHILD);
            when(authenticatedProvider.getCurrentMember()).thenReturn(member);

            when(newsRepository.findByCategoryOrderByPostedAt(any(Category.class), anyInt()))
                    .thenAnswer(inv -> {
                        Category category = inv.getArgument(0);
                        int count = inv.getArgument(1);
                        return allNews.stream()
                                .filter(n -> n.getCategory() == category)
                                .sorted(Comparator.comparing(News::getPostedAt).reversed())
                                .limit(count)
                                .toList();
                    });

            // when
            var result = newsService.getRecommend();

            assertThat(result).hasSize(3);
            assertThat(result).extracting(NewsPreviewResponseDto::getTitle)
                    .containsAnyOf("C-1","C-2","H-1");
        }

        @Test
        void 관심사가_3개_이상일_때의_테스트() {
            News n1 = createNews(Category.HEALTH, "H-1", LocalDate.now());
            News n2 = createNews(Category.CHILD, "C-1", LocalDate.now());
            News n3 = createNews(Category.FINANCIAL, "F-1", LocalDate.now());
            News n4 = createNews(Category.PREGNANCY_PLANNING, "PP-1", LocalDate.now());
            List<News> allNews = List.of(n1, n2, n3, n4);

            Member member = saveMemberWithInterests(Category.HEALTH, Category.CHILD, Category.FINANCIAL, Category.PREGNANCY_PLANNING);
            when(authenticatedProvider.getCurrentMember()).thenReturn(member);

            when(newsRepository.findByCategoryOrderByPostedAt(any(Category.class), anyInt()))
                    .thenAnswer(inv -> {
                        Category category = inv.getArgument(0);
                        int count = inv.getArgument(1);
                        return allNews.stream()
                                .filter(n -> n.getCategory() == category)
                                .sorted(Comparator.comparing(News::getPostedAt).reversed())
                                .limit(count)
                                .toList();
                    });
            // when
            var result = newsService.getRecommend();

            // then
            assertThat(result).hasSize(3);
        }
    }
}