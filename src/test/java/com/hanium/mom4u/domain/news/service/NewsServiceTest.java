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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Transactional
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
        return memberRepository.save(m);
    }

    private News saveNews(Category category, String title, LocalDate postedAt) {
        News news = new News();
        news.setCategory(category);
        news.setTitle(title);
        news.setPostedAt(postedAt);
        return newsRepository.save(news);
    }

    @Nested
    class GetRecommend {

        @Test
        @DisplayName("관심사가 비어있을 때의 테스트")
        void 관심사가_비어있을_때의_테스트() {
            // given
            for (Category c : Category.values()) {
                saveNews(c, c.name()+"-1", LocalDate.now().minusDays(2));
                saveNews(c, c.name()+"-2", LocalDate.now().minusDays(1));
            }
            Member member = memberWithNone();
            when(authenticatedProvider.getCurrentMember()).thenReturn(member);

            // when
            List<NewsPreviewResponseDto> result = newsService.getRecommend();

            assertThat(result).hasSize(Math.min(3, Category.values().length));
            // 각 DTO가 실제 저장된 뉴스의 타이틀을 가지고 오는지 간단 확인
            assertThat(result).allMatch(dto -> dto.getTitle() != null);
        }

        @Test
        @DisplayName("관심사가 하나일 때의 테스트")
        void 관심사가_하나일_때의_테스트() {
            saveNews(Category.EMOTIONAL, "E-1", LocalDate.now().minusDays(3));
            saveNews(Category.EMOTIONAL, "E-2", LocalDate.now().minusDays(2));
            saveNews(Category.EMOTIONAL, "E-3", LocalDate.now().minusDays(1));

            Member member = saveMemberWithInterests(Category.EMOTIONAL);
            when(authenticatedProvider.getCurrentMember()).thenReturn(member);

            var result = newsService.getRecommend();

            assertThat(result).hasSize(3);
            assertThat(result).extracting(NewsPreviewResponseDto::getTitle)
                    .containsExactlyInAnyOrder("E-1","E-2","E-3");
        }

        @Test
        @DisplayName("관심사가 2개일 때의 테스트")
        void 관심사가_2개일_때의_테스트() {
            // 어떤 카테고리에 2개/1개가 갈지는 shuffle에 의존하므로 개수만 검증
            saveNews(Category.CHILD, "C-1", LocalDate.now().minusDays(2));
            saveNews(Category.CHILD, "C-2", LocalDate.now().minusDays(1));
            saveNews(Category.HEALTH, "H-1", LocalDate.now().minusDays(1));

            Member member = saveMemberWithInterests(Category.HEALTH, Category.CHILD);
            when(authenticatedProvider.getCurrentMember()).thenReturn(member);

            var result = newsService.getRecommend();

            assertThat(result).hasSize(3);
            assertThat(result).extracting(NewsPreviewResponseDto::getTitle)
                    .containsAnyOf("C-1","C-2","H-1");
        }

        @Test
        void 관심사가_3개_이상일_때의_테스트() {
            saveNews(Category.HEALTH, "H-1", LocalDate.now());
            saveNews(Category.CHILD, "C-1", LocalDate.now());
            saveNews(Category.FINANCIAL, "F-1", LocalDate.now());
            saveNews(Category.PREGNANCY_PLANNING, "PP-1", LocalDate.now());

            Member member = saveMemberWithInterests(Category.HEALTH, Category.CHILD, Category.FINANCIAL, Category.PREGNANCY_PLANNING);
            when(authenticatedProvider.getCurrentMember()).thenReturn(member);
            var result = newsService.getRecommend();

            assertThat(result).hasSize(3);
        }
    }
}