package com.aaronjosh.real_estate_app.util;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

@Component
public class CloudflareR2Service {

    @Value("${CLOUDFLARE_R2_ACCESS_KEY}")
    private String accessKey;

    @Value("${CLOUDFLARE_R2_SECRET_KEY}")
    private String secretKey;

    @Value("${CLOUDFLARE_R2_ENDPOINT}")
    private String endpoint;

    @Value("${CLOUDFLARE_R2_BUCKET_NAME}")
    private String bucketName;

    @Value("${CLOUDFLARE_R2_PUBLIC_URL}")
    private String publicUrl;

    private S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(serviceConfiguration)
                .region(Region.US_EAST_1)
                .build();
    }

    public String uploadFile(MultipartFile file, boolean isPublic) throws IOException {
        S3Client s3 = s3Client();

        // Check bucket exists
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            s3.createBucket(b -> b.bucket(bucketName));
        }

        // Generate unique key for file
        String key = "uploads/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType());

        // Make file public if requested
        if (isPublic) {
            requestBuilder.acl("public-read");
        }

        s3.putObject(requestBuilder.build(), RequestBody.fromBytes(file.getBytes()));

        return key;
    }
}