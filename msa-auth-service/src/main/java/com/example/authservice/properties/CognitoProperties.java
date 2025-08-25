package com.example.authservice.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "aws.cognito")
public class CognitoProperties {
    private String userPoolId;
    private String appClientId;
    private String appClientSecret;
    private Groups groups = new Groups();

    public static class Groups {
        private String admin;
        private String owner;
        private String customer;
    }
}
