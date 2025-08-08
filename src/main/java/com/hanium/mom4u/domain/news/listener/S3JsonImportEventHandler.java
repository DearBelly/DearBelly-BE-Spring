package com.hanium.mom4u.domain.news.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final String NEWS_KEY = "data/preprocessed/";

    @EventListener
    public void importJsonFromS3(S3JsonImportEvent event) {

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(event.getBucketName())
                .prefix(NEWS_KEY)
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

                newsJdbcRepository.save(dto);
                System.out.printf("PostId: %s 저장 성공\n", dto.getPostId());
            } catch (IOException e) {
                throw new GeneralException(StatusCode.JSON_PARSING_ERROR);
            }
        }
    }
}
