package com.hanium.mom4u.domain.news.repository;

import com.hanium.mom4u.domain.news.entity.NewsBookmark;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsBookmarkRepository extends JpaRepository<NewsBookmark, Long> {

    boolean existsByMember_IdAndNews_Id(Long memberId, Long newsId);

    void deleteByMember_IdAndNews_Id(Long memberId, Long newsId);


}
