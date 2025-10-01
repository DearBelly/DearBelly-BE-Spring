package com.hanium.mom4u.domain.question.controller;

import com.hanium.mom4u.domain.question.service.QuestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name = "질문 API Controller", description = "질문 관련 API Controller입니다.")
public class QuestionController {

    private final QuestionService questionService;
}
