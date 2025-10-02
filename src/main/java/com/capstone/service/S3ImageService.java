package com.capstone.service;

public interface S3ImageService {
    String generatePresignedUrl(String imageKey);
}
