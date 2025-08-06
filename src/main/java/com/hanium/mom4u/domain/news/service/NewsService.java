package com.hanium.mom4u.domain.news.service;

import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.news.dto.response.NewsPreviewResponseDto;
import com.hanium.mom4u.domain.news.repository.NewsRepository;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final AuthenticatedProvider authenticatedProvider;
    private final NewsRepository newsRepository;

    // 정보 추천 별 보여주기(3개)
    public List<NewsPreviewResponseDto> getRecommend() {
        Member member = authenticatedProvider.getCurrentMember();
        return null;
    }

    // 정보 카테고리 별 대표 하나씩 보여주기
    public List<NewsPreviewResponseDto> getNewsPerCategory() {
        return null;
    }

    // 정보 카테고리 별 보여주기 및 전체 보여주기
    public Slice<NewsPreviewResponseDto> getAllNewsPerCateogry() {
        return null;
    }
}
