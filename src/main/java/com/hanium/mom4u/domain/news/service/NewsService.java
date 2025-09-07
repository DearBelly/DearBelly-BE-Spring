package com.hanium.mom4u.domain.news.service;

import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.dto.response.NewsDetailResponseDto;
import com.hanium.mom4u.domain.news.dto.response.NewsPreviewResponseDto;
import com.hanium.mom4u.domain.news.entity.News;
import com.hanium.mom4u.domain.news.entity.NewsBookmark;
import com.hanium.mom4u.domain.news.repository.NewsBookmarkRepository;
import com.hanium.mom4u.domain.news.repository.NewsRepository;
import com.hanium.mom4u.global.exception.BusinessException;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final AuthenticatedProvider authenticatedProvider;
    private final NewsRepository newsRepository;
    private final NewsBookmarkRepository bookmarkRepository;

    private Long tryGetMemberId() {
        try { return authenticatedProvider.getCurrentMemberId(); }
        catch (Exception e) { return null; } // 비로그인/만료 등 → null
    }
    private Member tryGetMember() {
        try { return authenticatedProvider.getCurrentMember(); }
        catch (Exception e) { return null; } // 비로그인/만료 등 → null
    }

    @Transactional(readOnly = true)
    public List<NewsPreviewResponseDto> getRecommend() {
        Member member = tryGetMember();            // 비로그인 허용
        Long meId = tryGetMemberId();              // null 가능

        List<Category> interests = (member == null)
                ? Collections.emptyList()
                : new ArrayList<>(member.getInterests());

        List<NewsPreviewResponseDto> recommendations = new ArrayList<>(3); // 3개 고정
        Map<Category, Integer> map = new LinkedHashMap<>();
        List<Category> categories = new ArrayList<>(List.of(Category.values()));
        Collections.shuffle(categories);

        if (interests.isEmpty()) {
            List<Category> selected = categories.subList(0, Math.min(3, categories.size()));
            for (Category category : selected) map.put(category, 1);
        } else if (interests.size() == 1) {
            map.put(interests.get(0), 3);
        } else if (interests.size() == 2) {
            Collections.shuffle(interests);
            Category first = interests.get(0);
            Category second = interests.get(1);
            map.put(first, 2);
            map.put(second, 1);
        } else {
            Collections.shuffle(interests);
            List<Category> selected = interests.subList(0, Math.min(3, interests.size()));
            for (Category category : selected) map.put(category, 1);
        }

        for (Map.Entry<Category, Integer> entry : map.entrySet()) {
            Category category = entry.getKey();
            int count = entry.getValue();

            List<News> newsList = newsRepository.findByCategoryOrderByPostedAt(category, count);

            recommendations.addAll(
                    newsList.stream()
                            .map(n -> NewsPreviewResponseDto.toPreviewDto(
                                    n, meId != null && newsRepository.isBookmarked(meId, n.getId())
                            ))
                            .toList()
            );
        }
        return recommendations;
    }

    @Transactional(readOnly = true)
    public List<NewsPreviewResponseDto> getNewsPerCategory() {
        Long meId = tryGetMemberId(); // null 가능
        return newsRepository.findLatestIdByCategory()
                .stream()
                .map(n -> NewsPreviewResponseDto.toPreviewDto(
                        n, meId != null && newsRepository.isBookmarked(meId, n.getId())
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Slice<NewsPreviewResponseDto> getAllNewsPerCategory(int index, int page) {
        final int PAGE_SIZE = 15;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Long meId = tryGetMemberId(); // null 가능

        try {
            if (index == 0) {
                Slice<News> newsList = newsRepository.findAllOrderByPostId(pageable);
                return newsList.map(n -> NewsPreviewResponseDto.toPreviewDto(
                        n, meId != null && newsRepository.isBookmarked(meId, n.getId())
                ));
            } else {
                Slice<News> newsList = newsRepository.findByCategory(Category.getCategory(index), pageable);
                return newsList.map(n -> NewsPreviewResponseDto.toPreviewDto(
                        n, meId != null && newsRepository.isBookmarked(meId, n.getId())
                ));
            }
        } catch (Exception e) {
            throw new BusinessException(StatusCode.NEWS_OUT_OF_INDEX);
        }
    }

    @Transactional(readOnly = true)
    public NewsDetailResponseDto getDetail(Long newsId) {
        Long meId = tryGetMemberId(); // null 가능

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new GeneralException(StatusCode.NEWS_NOT_FOUND));

        boolean bookmarked = (meId != null) && newsRepository.isBookmarked(meId, newsId);

        return NewsDetailResponseDto.toDetailDto(news, bookmarked);
    }

    @Transactional
    public void addBookmark(Long newsId) {
        var me = authenticatedProvider.getCurrentMember();
        if (bookmarkRepository.existsByMember_IdAndNews_Id(me.getId(), newsId)) return;
        var news = newsRepository.findById(newsId)
                .orElseThrow(() -> new GeneralException(StatusCode.NEWS_NOT_FOUND));
        bookmarkRepository.save(new NewsBookmark(me, news));
    }


    @Transactional
    public void removeBookmark(Long newsId) {
        var me = authenticatedProvider.getCurrentMember();
        bookmarkRepository.deleteByMember_IdAndNews_Id(me.getId(), newsId);
    }



    @Transactional(readOnly = true)
    public Slice<NewsPreviewResponseDto> getMyBookmarks(int page, int size) {
        var me = authenticatedProvider.getCurrentMember();
        var pageable = PageRequest.of(page, size);
        return newsRepository.findMyBookmarks(me.getId(), pageable)
                .map(n -> NewsPreviewResponseDto.toPreviewDto(n, true));
    }
}
