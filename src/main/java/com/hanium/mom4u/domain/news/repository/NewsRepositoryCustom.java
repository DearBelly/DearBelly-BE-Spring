package com.hanium.mom4u.domain.news.repository;

import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.entity.News;

import java.util.List;

public interface NewsRepositoryCustom {

    // 카테고리 별로 하나씩 보여주기
    List<News> findLatestIdByCategory();
    List<News> findByCategoryOrderByPostedAt(Category category, int count);
}
