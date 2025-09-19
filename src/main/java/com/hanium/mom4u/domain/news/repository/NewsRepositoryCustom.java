package com.hanium.mom4u.domain.news.repository;

import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.entity.News;

import java.util.List;

public interface NewsRepositoryCustom {

    // 카테고리 별로 하나씩 보여주기
    List<News> findLatestIdByCategory();
    // 관심 카테고리 지정에 따라 보여주는 것(최신순)
    List<News> findByCategoryOrderByPostedAt(Category category, int count);
    // 카테고리 별 반환(최신순)
    List<News> findAllByCategoryOrderByPostedAt(Category category);
}
