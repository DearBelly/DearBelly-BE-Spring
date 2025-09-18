package com.hanium.mom4u.domain.news.listener;

import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.entity.News;
import com.hanium.mom4u.domain.news.repository.NewsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsScheduler {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    private final ApplicationEventPublisher eventPublisher;

    private final NewsRepository newsRepository;

    private static final Map<Category, List<String>> newsMap = new HashMap<>();
    @Value("${spring.cloud.aws.s3.image-prefix}")
    private String imagePrefix;
    private static final String EXTENSION = ".png";

    @PostConstruct
    private void setNewsMap() {
        for (Category category : Category.values()) {
            List<String> imageUrl = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                imageUrl.add(imagePrefix + category.name() + i + EXTENSION);
            }
            newsMap.put(category, imageUrl);
        }
    }

    @Async("schedulerExecutor")
    @Scheduled(cron = "0 0 4 * * *") // 새벽 4시에 실행
    public void triggerSaveEvent() {
        log.info("S3 Importing Event started");
        eventPublisher.publishEvent(new S3JsonImportEvent(this, BUCKET_NAME));
    }
}
