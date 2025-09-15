package com.hanium.mom4u.external.s3.service;

import com.hanium.mom4u.external.s3.dto.response.S3ScanFolderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;


import java.io.IOException;
import java.net.URL;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.s3.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.s3.secret-key}")
    private String secretKey;

    private final static String S3_IMG_PATH = "scan/";

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


    /**
     * 다운로드용 PresignedUrl
     * @param objectKey
     * @return
     */
    public String generatePresignedGetUrl(String objectKey) {
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

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(3))
                .build();

        URL presignedGetUrl = presigner.presignGetObject(presignRequest).url();
        presigner.close();

        return presignedGetUrl.toString();
    }

    /**
     * S3에 받은 이미지를 업로드 후 PresignedURL 반환
     * 업로드한 이미지의 이름은 JOB의 CorrelationID를 사용
     */
    public S3ScanFolderResponseDto uploadFileAndGetPresignedUrl(
            MultipartFile multipartFile, String correlationId) throws IOException {

        String key = S3_IMG_PATH + correlationId + "_" + multipartFile.getOriginalFilename();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(multipartFile.getContentType())
                .build();

        s3Client.putObject(
                objectRequest,
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                        multipartFile.getInputStream(), multipartFile.getSize()
                )
        );

        String presignedUrl = generatePresignedGetUrl(key);
        log.info("업로드 및 presigned GET URL 발급 성공: {}", presignedUrl);

        return S3ScanFolderResponseDto.builder()
                .objectKey(key)
                .presignedUrl(presignedUrl)
                .build();
    }
}
