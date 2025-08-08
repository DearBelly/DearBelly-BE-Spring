package com.hanium.mom4u.domain.member.dto.request;

import com.hanium.mom4u.domain.news.common.Category;
import lombok.Getter;
import java.util.Set;

@Getter
public class CategoryUpdateRequest {

    private Set<Category> categories;
}