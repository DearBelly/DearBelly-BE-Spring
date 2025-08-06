package com.hanium.mom4u.domain.news.dto.response;

import com.hanium.mom4u.domain.news.common.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "정보 목록에서 조회되는 DTO")
public class NewsPreviewResponseDto {

    private Long newsId;
    private String title;
    private String subTitle;
    private String imageUrl;
    private Category contentType;
}
