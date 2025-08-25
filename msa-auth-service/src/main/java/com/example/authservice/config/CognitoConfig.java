package com.example.authservice.config;

import com.example.authservice.properties.AwsRegionProperties;
import com.example.authservice.properties.CognitoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({AwsRegionProperties.class, CognitoProperties.class})
@RequiredArgsConstructor
public class CognitoConfig {

    private final AwsRegionProperties regionProps;

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient(
            @Value("${aws.credentials.access-key}") String accessKey,
            @Value("${aws.credentials.secret-key}") String secretKey
    ) {
        AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);

        ClientOverrideConfiguration override = ClientOverrideConfiguration.builder()
                .retryPolicy(RetryMode.STANDARD)
                .apiCallTimeout(Duration.ofSeconds(10))
                .apiCallAttemptTimeout(Duration.ofSeconds(10))
                // ClockSkew 보정이 필요하면 아래 옵션을 사용할 수도 있음 (서버 시계가 틀릴 때)
                .putAdvancedOption(SdkAdvancedClientOption.SIGNER, null)
                .build();

        return CognitoIdentityProviderClient.builder()
                .region(Region.of(regionProps.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .overrideConfiguration(override)
                .build();
    }
}
