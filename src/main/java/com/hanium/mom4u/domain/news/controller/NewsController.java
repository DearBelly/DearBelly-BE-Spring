package com.hanium.mom4u.domain.news.controller;

import com.hanium.mom4u.domain.news.service.NewsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news")
@Tag(name = "정보 관련 API Controller", description = "정보 관련 API Controller입니다")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    // 정보 추천 별 보여주기(3개)

    // 정보 카테고리 별 대표 하나씩 보여주기

    // 정보 카테고리 별 보여주기 및 전체 보여주기

}
