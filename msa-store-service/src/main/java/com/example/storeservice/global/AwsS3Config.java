//package com.example.storeservice.global;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.AwsCredentials;
//import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.presigner.S3Presigner;
//
//@Configuration
//public class AwsS3Config {
//    @Value("${spring.cloud.aws.credentials.accessKey}")
//    private String accessKey;
//
//    @Value("${spring.cloud.aws.credentials.secretKey}")
//    private String accessSecret;
//
//    @Value("${spring.cloud.aws.region.static:ap-northeast-2}") // 안전 디폴트
//    private String region;
//
//    @Bean
//    public AwsCredentialsProvider awsCredentialsProvider() {
//        return StaticCredentialsProvider.create(
//                AwsBasicCredentials.create(accessKey, accessSecret)
//        );
//    }
//
//    @Bean
//    public Region awsRegion() {
//        return Region.of(region);
//    }
//
//    @Bean
//    public S3Client s3Client(Region region, AwsCredentialsProvider creds) {
//        return S3Client.builder()
//                .region(region)
//                .credentialsProvider(creds)
//                .build();
//    }
//
//    @Bean
//    public S3Presigner s3Presigner(Region region, AwsCredentialsProvider creds) {
//        return S3Presigner.builder()
//                .region(region)
//                .credentialsProvider(creds)
//                .build();
//    }
//}
