package com.hanium.mom4u.domain.news.repository;

import com.hanium.mom4u.global.crawling.dto.CrawlingResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class NewsJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    public void save(CrawlingResultDto dto) {

        // 중복 체크
        String findQuery = "SELECT EXISTS (SELECT 1 FROM news WHERE post_id = ?)";
        Integer count = jdbcTemplate.queryForObject(findQuery, Integer.class, dto.getPostId());

        if (count != null && count > 0) {
            // 이미 존재
            System.out.printf("이미 존재하는 post_id: %s, 저장 생략\n", dto.getPostId());
            return;
        }

        // 데이터 삽입
        String sql = "INSERT INTO news (post_id, title, sub_title, link, content, img_url, posted_at) " +
                "VALUES (? ,?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                dto.getPostId(),
                dto.getTitle(),
                dto.getSubTitle(),
                dto.getLink(),
                dto.getContent(),
                dto.getImageUrl(),
                dto.getPostedAt());
    }
}
