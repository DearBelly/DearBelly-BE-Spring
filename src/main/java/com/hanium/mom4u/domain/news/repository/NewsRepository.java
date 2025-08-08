package com.hanium.mom4u.domain.news.repository;

import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.entity.News;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long>, NewsRepositoryCustom {

    @Query("SELECT n FROM News n where n.category = :category")
    Slice<News> findByCategory(@Param("category") Category category, Pageable pageable);

    Optional<News> findById(Long newsId);

    @Query("SELECT n FROM News n ORDER BY n.postId DESC ")
    Slice<News> findAllOrderByPostId(Pageable pageable);
}
