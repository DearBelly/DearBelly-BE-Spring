package com.hanium.mom4u.domain.news.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.repository.NewsJdbcRepository;
import com.hanium.mom4u.global.crawling.dto.CrawlingResultDto;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class S3JsonImportEventHandler {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final NewsJdbcRepository newsJdbcRepository;
    private static final String[] NEWS_KEY = {
            "data/HEALTH/preprocessed/", "data/FINANCIAL/preprocessed/", "data/PREGNANCY_PLANNING/preprocessed/",
            "data/CHILD/preprocessed/", "data/EMOTIONAL/preprocessed/"
    };

    @EventListener
    public void importJsonFromS3(S3JsonImportEvent event) {

        for (int i = 1; i <= NEWS_KEY.length; i++) {
            String newsKey = NEWS_KEY[i-1];

            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(event.getBucketName())
                    .prefix(newsKey)
                    .build();
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            List<S3Object> objectList = listResponse.contents();

            for (S3Object s3Object : objectList) {
                String key = s3Object.key();

                if (!key.endsWith(".json")) {
                    continue; // skip
                }
                ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build());

                try {
                    CrawlingResultDto dto = objectMapper.readValue(s3InputStream, CrawlingResultDto.class);

                    Category cat = Category.getCategory(i);
                    newsJdbcRepository.save(dto, cat);
                    System.out.printf("카테고리 %s 중 PostId: %s 저장 성공\n", newsKey, dto.getPostId());
                } catch (IOException e) {
                    throw new GeneralException(StatusCode.JSON_PARSING_ERROR);
                }
            }
        }
    }
}
