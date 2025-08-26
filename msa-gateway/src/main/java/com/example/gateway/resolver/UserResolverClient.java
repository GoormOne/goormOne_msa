package com.example.gateway.resolver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserResolverClient {

    private final WebClient.Builder lbWebClientBuilder;

    private static final String USER_SERVICE_BASE = "lb://msa-user-service";
    private static final Duration RESOLVE_TIMEOUT = Duration.ofMillis(1500);

    public ResolveRes resolve(String username, String email, String group, String bearer) {
        WebClient client = lbWebClientBuilder.baseUrl(USER_SERVICE_BASE).build();

        return client.get()
                .uri(uri -> uri.path("/internal/users/resolve")
                        .queryParam("username", username)
                        .queryParam("email", email)
                        .queryParam("group", group)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearer == null ? "" : bearer)
                .retrieve()
                .bodyToMono(ResolveRes.class)
                .timeout(RESOLVE_TIMEOUT)
                .block();
    }

    public static class ResolveRes {
        public String principalType;
        public String userId;
        public String username;
        public String email;
        public List<String> roles;
    }
}