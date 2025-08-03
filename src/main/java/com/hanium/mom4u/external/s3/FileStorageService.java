package com.hanium.mom4u.external.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.domain.news.entity.News;
import com.hanium.mom4u.domain.news.repository.NewsRepository;
import com.hanium.mom4u.global.crawling.dto.CrawlingResultDto;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;


import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.s3.access-key}") // optional - 로컬에서만 필요
    private String accessKey;

    @Value("${spring.cloud.aws.s3.secret-key}") // optional - 로컬에서만 필요
    private String secretKey;


    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final NewsRepository newsRepository;
    private static final String NEWS_KEY = "data/preprocessed/";

    public String generatePresignedPutUrl(String objectKey) {
        S3Presigner presigner;

        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            presigner = S3Presigner.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(region))
                    .build();
        } else {
            presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .build();
        }

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(objectRequest)
                .signatureDuration(Duration.ofMinutes(5))
                .build();

        URL presignedPutUrl = presigner.presignPutObject(presignRequest).url();
        presigner.close();

        return presignedPutUrl.toString();
    }


    public void deleteFile(String objectKey) {
        S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        (accessKey != null && secretKey != null) ?
                                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)) :
                                DefaultCredentialsProvider.create()
                )
                .build();

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        s3Client.deleteObject(deleteRequest);
        s3Client.close();
    }

    @EventListener
    public void importJsonFromS3(String bucketName) {

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(NEWS_KEY)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

        List<S3Object> objectList = listResponse.contents();

        for (S3Object s3Object: objectList) {
            String key = s3Object.key();

            if (!key.endsWith(".json")) {
                continue; // skip
            }
            ResponseInputStream<GetObjectResponse> s3InputStream =  s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

            try {
                CrawlingResultDto dto = objectMapper.readValue(s3InputStream, CrawlingResultDto.class);

                News news = CrawlingResultDto.toEntity(dto);
                newsRepository.save(news);
                System.out.printf("{} 저장 성공", news.getId());
            } catch (IOException e) {
                throw new GeneralException(StatusCode.JSON_PARSING_ERROR);
            }
        }
    }
}
