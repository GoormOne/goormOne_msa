package com.example.authservice.controller;

import com.example.authservice.properties.CognitoProperties;
import com.example.authservice.properties.AwsRegionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;


@RestController
@RequestMapping("/auth/health")
public class HealthController {
    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final CognitoIdentityProviderClient client;
    private final CognitoProperties cognitoProps;
    private final AwsRegionProperties regionProps;
    private final String accessKey;
    private final String secretKey;

    public HealthController(CognitoIdentityProviderClient client,
                            CognitoProperties cognitoProps,
                            AwsRegionProperties regionProps,
                            @Value("${aws.credentials.access-key:}") String accessKey,
                            @Value("${aws.credentials.secret-key:}") String secretKey) {
        this.client = client;
        this.cognitoProps = cognitoProps;
        this.regionProps = regionProps;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    // env 로딩 확인
    @GetMapping("/env")
    public String envEcho() {
        String masked = secretKey == null ? "null" :
                (secretKey.length() < 4 ? "****" : "****" + secretKey.substring(secretKey.length()-4));
        return "region=" + regionProps.getRegion() + "\n"
                + "userPoolId=" + cognitoProps.getUserPoolId() + "\n"
                + "appClientId=" + cognitoProps.getAppClientId() + "\n"
                + "accessKey=" + accessKey + "\n"
                + "secretKey(endsWith)=" + masked;
    }

    // 키가 유효한지 확인
    @GetMapping("/sts")
    public String whoAmI() {
        try (StsClient sts = StsClient.builder()
                .region(Region.of(regionProps.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build()) {

            GetCallerIdentityResponse r = sts.getCallerIdentity();
            return "STS OK: account=" + r.account() + ", arn=" + r.arn();
        } catch (Exception e) {
            return "STS ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/cognito")
    public String checkCognito() {
        try {
            ListUsersResponse res = client.listUsers(
                    ListUsersRequest.builder()
                            .userPoolId(cognitoProps.getUserPoolId())
                            .limit(1)
                            .build()
            );
            int count = res.users() == null ? 0 : res.users().size();
            return "OK: listUsers ok, sampleCount=" + count;
        } catch (CognitoIdentityProviderException e) {
            // AWS SDK 예외는 여기서 대부분 원인 파악 가능
            String msg = "[COGNITO_ERROR] code=" + e.awsErrorDetails().errorCode() +
                    ", message=" + e.awsErrorDetails().errorMessage();
            log.error(msg, e);
            return msg; // 임시로 바로 반환(원인 식별용)
        } catch (Exception e) {
            log.error("[UNKNOWN_ERROR] " + e.getMessage(), e);
            return "[UNKNOWN_ERROR] " + e.getMessage();
        }
    }
}
