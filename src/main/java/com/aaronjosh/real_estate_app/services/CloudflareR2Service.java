package com.aaronjosh.real_estate_app.services;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.aaronjosh.real_estate_app.models.FileEntity;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.ResponseInputStream;
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

    public String uploadFile(MultipartFile file, boolean isPublic, String location) throws IOException {
        S3Client s3 = s3Client();

        // Check bucket exists
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            s3.createBucket(b -> b.bucket(bucketName));
        }

        String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
        String key = location + UUID.randomUUID() + "-" + fileName;
        String contentType = file.getContentType() != null
                ? file.getContentType()
                : "application/octet-stream";

        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType);

        if (isPublic) {
            requestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
        } else {
            requestBuilder.acl(ObjectCannedACL.PRIVATE);
        }

        s3.putObject(requestBuilder.build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return key;
    }

    public ResponseInputStream<GetObjectResponse> loadFile(String key) {
        S3Client s3 = s3Client();

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3.getObject(request);

        } catch (S3Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "File not found in storage",
                    e);
        }
    }

    public String generateLink(FileEntity file) {
        if (file.getIsPublic()) {
            return publicUrl + "/" + file.getKey();
        }

        String domainUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .toUriString();

        return domainUrl + "/api/files/" + file.getId();
    }
}