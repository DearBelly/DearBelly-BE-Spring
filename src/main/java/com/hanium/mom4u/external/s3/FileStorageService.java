package com.hanium.mom4u.external.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;


import java.net.URL;
import java.time.Duration;

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
}
