package com.hanium.mom4u.global.crawling.dto;

import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.entity.News;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlingResultDto {

    private String postId;
    private String category;
    private String title;
    private String subTitle;
    private String link;
    private String content;
    private String imageUrl;
    private String postedAt;

    public static News toEntity(CrawlingResultDto dto) {
        Category category = null;
        if (category != null) {
            try {
                category = Category.valueOf(dto.getCategory().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.printf("{} 카테고리 잘못됨\n", dto.getPostId());
            }
        }

        return News.builder()
                .category(category)
                .title(dto.getTitle())
                .subTitle(dto.getSubTitle())
                .content(dto.getContent())
                .link(dto.getLink())
                .imgUrl(dto.getImageUrl())
                .build();
    }
}
