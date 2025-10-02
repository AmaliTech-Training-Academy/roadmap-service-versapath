package com.capstone.service.impl;

import com.capstone.service.S3ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ImageServiceImpl implements S3ImageService {

    private final S3Presigner s3Presigner;

    @Value("${AWS_BUCKET_NAME}")
    private String bucketName;

    @Value("${AWS_PRESIGNED_URL_EXPIRATION:3600}")
    private long urlExpiration;

    @Override
    public String generatePresignedUrl(String imageKey) {
        if (imageKey == null || imageKey.trim().isEmpty()) {
            return null;
        }

        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(urlExpiration))
                    .getObjectRequest(req -> req
                            .bucket(bucketName)
                            .key(imageKey))
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();

        } catch (Exception e) {
            log.error("Error generating presigned URL for image key: {}", imageKey, e);
            return null;
        }
    }
}
