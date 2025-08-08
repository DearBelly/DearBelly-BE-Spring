package com.hanium.mom4u.domain.news.service;

import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.dto.response.NewsDetailResponseDto;
import com.hanium.mom4u.domain.news.dto.response.NewsPreviewResponseDto;
import com.hanium.mom4u.domain.news.entity.News;
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

    // 정보 추천 별 보여주기(3개)
    @Transactional(readOnly = true)
    public List<NewsPreviewResponseDto> getRecommend() {
        Member member = authenticatedProvider.getCurrentMember();
        List<Category> interests = new ArrayList<>(member.getInterests());

        List<NewsPreviewResponseDto> recommendations = new ArrayList<>(3); // 3개 고정

        Map<Category, Integer> map = new LinkedHashMap<>();

        List<Category> categories = new ArrayList<>(List.of(Category.values()));
        Collections.shuffle(categories);

        // 회원이 관심있어하는 정보로 반환
        if (interests.isEmpty()) {
            // 랜덤으로 3개의 카테고리 선택
            List<Category> selected = categories.subList(0, Math.min(3, categories.size()));
            for (Category category : selected) {
                map.put(category, 1);
            }
        }
        else if (interests.size() == 1) { // 관심 카테고리가 1개
            Category category = interests.get(0);
            map.put(category, 3);
        }
        else if (interests.size() == 2) {
            Collections.shuffle(interests); // 2개 중 1개만 랜덤
            Category first = interests.get(0);
            Category second = interests.get(1);
            map.put(first, 2); // 2개 반환
            map.put(second, 1);
        }
        else {  // 관심 카테고리가 3개 이상인 경우
                Collections.shuffle(interests);
                List<Category> selected = interests.subList(0, Math.min(3, interests.size()));
                for (Category category : selected) {
                    map.put(category, 1);
                }
            }

        // 카테고리 별 할당 수만큼 반환
        for (Map.Entry<Category, Integer> entry: map.entrySet()) {
            Category category = entry.getKey();
            int count = entry.getValue(); // 반환해야하는 카테고리 별 개수


            List<News> newsList = newsRepository.findByCategoryOrderByPostedAt(
                    category, count
            );

            recommendations
                    .addAll(
                            newsList.stream()
                            .map(NewsPreviewResponseDto::toPreviewDto)
                            .toList()
                    );
            for (NewsPreviewResponseDto dto : recommendations) {
                log.info(dto.getTitle());
            }
        }
        return recommendations;
    }

    @Transactional(readOnly = true)
    // 정보 카테고리 별 대표 하나씩 보여주기
    public List<NewsPreviewResponseDto> getNewsPerCategory() {
        return newsRepository.findLatestIdByCategory()
                .stream()
                .map(NewsPreviewResponseDto::toPreviewDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    // 정보 카테고리 별 보여주기 및 전체 보여주기
    public Slice<NewsPreviewResponseDto> getAllNewsPerCategory(int index, int page) {

        final int PAGE_SIZE = 15;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        try {
            // 전체 조회하기
            if (index == 0) {
                Slice<News> newsList = newsRepository.findAllOrderByPostId(pageable);
                return newsList
                        .map(NewsPreviewResponseDto::toPreviewDto);
            }
            // 카테고리 별로 조회하기
            else {
                Category[] categories = Category.values();

                Slice<News> newsList = newsRepository.findByCategory(categories[index -1], pageable);
                return newsList
                        .map(NewsPreviewResponseDto::toPreviewDto);
            }
        } catch (Exception e) {
            throw new BusinessException(StatusCode.NEWS_OUT_OF_INDEX);
        }
    }

    @Transactional(readOnly = true)
    public NewsDetailResponseDto getDetail(Long newsId) {
        return newsRepository.findById(newsId)
                .map(NewsDetailResponseDto::toDetailDto)
                .orElseThrow(() -> new GeneralException(StatusCode.NEWS_NOT_FOUND));
    }
}
