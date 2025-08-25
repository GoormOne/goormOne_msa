package com.example.storeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
//@RequiredArgsConstructor
@Slf4j
public class AwsS3Service {
//    private final S3Client s3Client;
//    @Value("${spring.cloud.aws.s3.bucket}")
//    private String bucketName;
//    private final S3Presigner s3Presigner;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    // ★★★ Spring이 자동으로 만들어준 S3Client와 S3Presigner를 직접 주입받습니다 ★★★
    public AwsS3Service(S3Client s3Client, S3Presigner s3Presigner,
                        @Value("${spring.cloud.aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    public String uploadFile(MultipartFile multipartFile) {

        String fileName = UUID.randomUUID().toString();
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .contentType(multipartFile.getContentType())
                    .contentLength(multipartFile.getSize())
                    .contentDisposition("inline")
                    .key(fileName)
                    .build();
            RequestBody requestBody = RequestBody.fromBytes(multipartFile.getBytes());
            s3Client.putObject(putObjectRequest, requestBody);
        } catch (IOException e) {
            log.error("cannot upload image",e);
            throw new RuntimeException(e);
        }

        return fileName;
    }

    public URL getImageUrl(String objectKey, Duration ttl) {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .responseContentType("image/jpeg")
                .responseContentDisposition("inline")
                .build();

        PresignedGetObjectRequest req = s3Presigner.presignGetObject(b -> b
                .signatureDuration(ttl)
                .getObjectRequest(get));
            return req.url();
        }
    }





