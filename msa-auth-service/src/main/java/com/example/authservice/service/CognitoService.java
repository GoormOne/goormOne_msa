package com.example.authservice.service;

import com.example.authservice.exception.CognitoCreateException;
import com.example.authservice.properties.CognitoProperties;
import com.example.authservice.util.SecretHash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CognitoService {

    private final CognitoIdentityProviderClient client;
    private final CognitoProperties props;

    /**
     * 표준 가입 API (중복 제거된 단일 진입점)
     * - AdminCreateUser(SUPPRESS) → AdminSetUserPassword(permanent) → AdminAddUserToGroup
     * - 표준 속성: email, name, (옵션) birthdate(yyyy-MM-dd), email_verified=true
     * - ①의 정교한 예외 매핑 유지
     */
    public String createUserAndReturnSub(
            String username,
            String rawPassword,
            String email,
            String name,
            LocalDate birth,
            String groupName
    ) {
        return createUserAndReturnSub(username, rawPassword, email, name, birth, groupName, null, true);
    }

    /**
     * 확장형(옵션): 커스텀 속성까지 주입해야 하는 경우 사용
     * - 예: Map.of("custom:user_id", customerId.toString())
     * - emailVerified 정책도 호출부에서 제어 가능
     */
    // Cognito 유저 생성 + 비번 설정 + 그룹 매핑 후 sub 반환
    public String createUserAndReturnSub(
            String username,
            String rawPassword,
            String email,
            String name,
            LocalDate birth,          // null 허용
            String groupName,         // null/blank 허용
            Map<String, String> customAttrsToSet, // null 허용
            boolean emailVerified
    ) {
        try {
            // 1) 속성 구성(표준 + 커스텀)
            List<AttributeType> attrs = new ArrayList<>();
            attrs.add(AttributeType.builder().name("email").value(email).build());
            attrs.add(AttributeType.builder().name("name").value(name).build());
            attrs.add(AttributeType.builder().name("email_verified").value(Boolean.toString(emailVerified)).build());
            if (birth != null) {
                attrs.add(AttributeType.builder().name("birthdate").value(birth.toString()).build());
            }
            if (customAttrsToSet != null && !customAttrsToSet.isEmpty()) {
                customAttrsToSet.forEach((k, v) -> {
                    if (k != null && !k.isBlank() && v != null && !v.isBlank()) {
                        attrs.add(AttributeType.builder().name(k).value(v).build());
                    }
                });
            }

            // 2) 사용자 생성 (이메일 발송 억제)
            AdminCreateUserRequest createReq = AdminCreateUserRequest.builder()
                    .userPoolId(props.getUserPoolId())
                    .username(username)
                    .userAttributes(attrs)
                    .messageAction(MessageActionType.SUPPRESS)
                    .build();
            AdminCreateUserResponse createRes = client.adminCreateUser(createReq);
            log.info("[COGNITO] AdminCreateUser OK username={}, status={}",
                    username, createRes.user().userStatusAsString());

            // 3) 비밀번호 영구 설정
            AdminSetUserPasswordRequest pwReq = AdminSetUserPasswordRequest.builder()
                    .userPoolId(props.getUserPoolId())
                    .username(username)
                    .password(rawPassword)
                    .permanent(true)
                    .build();
            client.adminSetUserPassword(pwReq);
            log.info("[COGNITO] AdminSetUserPassword OK username={}", username);

            // 4) 그룹 매핑 (옵션)
            if (groupName != null && !groupName.isBlank()) {
                AdminAddUserToGroupRequest addReq = AdminAddUserToGroupRequest.builder()
                        .userPoolId(props.getUserPoolId())
                        .username(username)
                        .groupName(groupName)
                        .build();
                client.adminAddUserToGroup(addReq);
                log.info("[COGNITO] AdminAddUserToGroup OK username={}, group={}", username, groupName);
            }

            // 5) sub 조회
            AdminGetUserResponse getRes = client.adminGetUser(
                    AdminGetUserRequest.builder()
                            .userPoolId(props.getUserPoolId())
                            .username(username)
                            .build()
            );

            String sub = getRes.userAttributes().stream()
                    .filter(a -> "sub".equals(a.name()))
                    .map(AttributeType::value)
                    .findFirst()
                    .orElseThrow(() -> new CognitoCreateException(HttpStatus.BAD_GATEWAY, "COGNITO_SUB_NOT_FOUND"));

            log.info("[COGNITO] sub resolved username={}, sub={}", username, sub);
            return sub;

        } catch (UsernameExistsException e) {
            // 이미 존재하는 계정
            throw new CognitoCreateException(HttpStatus.CONFLICT, "COGNITO_USERNAME_EXISTS");
        } catch (InvalidPasswordException e) {
            throw new CognitoCreateException(HttpStatus.BAD_REQUEST, "COGNITO_INVALID_PASSWORD");
        } catch (InvalidParameterException e) {
            throw new CognitoCreateException(HttpStatus.BAD_REQUEST, "COGNITO_INVALID_PARAMETER");
        } catch (TooManyRequestsException e) {
            throw new CognitoCreateException(HttpStatus.TOO_MANY_REQUESTS, "COGNITO_RATE_LIMITED");
        } catch (NotAuthorizedException e) {
            throw new CognitoCreateException(HttpStatus.FORBIDDEN, "COGNITO_NOT_AUTHORIZED");
        } catch (UserLambdaValidationException e) {
            throw new CognitoCreateException(HttpStatus.BAD_GATEWAY, "COGNITO_LAMBDA_VALIDATION_FAILED");
        } catch (Exception e) {
            log.error("[COGNITO] createUserAndReturnSub failed: {}", e.getMessage(), e);
            throw new CognitoCreateException(HttpStatus.BAD_GATEWAY, "COGNITO_CREATE_FAILED");
        }
    }

    public AuthenticationResultType login(String username, String password) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", username);
        authParams.put("PASSWORD", password);
        String secretHash = SecretHash.calculate(username, props.getAppClientId(), props.getAppClientSecret());
        if (secretHash != null) authParams.put("SECRET_HASH", secretHash);

        AdminInitiateAuthRequest req = AdminInitiateAuthRequest.builder()
                .userPoolId(props.getUserPoolId())
                .clientId(props.getAppClientId())
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(authParams)
                .build();

        AdminInitiateAuthResponse res = client.adminInitiateAuth(req);

//        if (res.challengeName() != null && res.challengeName() != ChallengeNameType.UNKNOWN_TO_SDK_VERSION) {
//            // NEW_PASSWORD_REQUIRED 등 대응 필요 시 여기에 처리
//            throw new NotAuthorizedException("Unsupported challenge: " + res.challengeName(), null);
//        }

        return res.authenticationResult();
    }

    public AuthenticationResultType refresh(String username, String refreshToken) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("REFRESH_TOKEN", refreshToken);
        String secretHash = SecretHash.calculate(username, props.getAppClientId(), props.getAppClientSecret());
        if (secretHash != null) authParams.put("SECRET_HASH", secretHash);

        AdminInitiateAuthRequest req = AdminInitiateAuthRequest.builder()
                .userPoolId(props.getUserPoolId())
                .clientId(props.getAppClientId())
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH) // Admin API도 지원
                .authParameters(authParams)
                .build();

        AdminInitiateAuthResponse res = client.adminInitiateAuth(req);
        return res.authenticationResult();
    }

    public void globalSignOut(String username) {
        AdminUserGlobalSignOutRequest req = AdminUserGlobalSignOutRequest.builder()
                .userPoolId(props.getUserPoolId())
                .username(username)
                .build();
        client.adminUserGlobalSignOut(req);
        log.info("[Cognito] AdminUserGlobalSignOut ok, username={}", username);
    }

    public static String[] extractGroups(String idToken) {
        // 토큰 파싱은 GW에서 검증/파싱하지만, 개발단계 편의를 위해 클레임만 간단 파싱 (Base64 JSON)
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) return new String[0];
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            // 매우 간단한 파싱 (정식 JSON 파서 사용 권장)
            int idx = payloadJson.indexOf("\"cognito:groups\"");
            if (idx < 0) return new String[0];
            int start = payloadJson.indexOf("[", idx);
            int end   = payloadJson.indexOf("]", start);
            if (start < 0 || end < 0) return new String[0];
            String arr = payloadJson.substring(start+1, end);
            String[] raw = arr.split(",");
            List<String> groups = new ArrayList<>();
            for (String s : raw) {
                String g = s.trim().replaceAll("^\"|\"$", "");
                if (!g.isEmpty()) groups.add(g);
            }
            return groups.toArray(new String[0]);
        } catch (Exception e) {
            return new String[0];
        }
    }

    // ===== 운영/테스트용 API 유지 =====

//    public AdminInitiateAuthResponse loginByAdminNoSrp(String username, String password) {
//        log.info("[COGNITO] login start username={}", username);
//        return client.adminInitiateAuth(AdminInitiateAuthRequest.builder()
//                .userPoolId(props.getUserPoolId())
//                .clientId(props.getAppClientId())
//                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
//                .authParameters(Map.of("USERNAME", username, "PASSWORD", password))
//                .build());
//    }
//
//    public AdminInitiateAuthResponse login(String username, String password) {
//        log.info("[COGNITO] login start username={}", username);
//        return client.adminInitiateAuth(AdminInitiateAuthRequest.builder()
//                .userPoolId(props.getUserPoolId())
//                .clientId(props.getAppClientId())
//                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
//                .authParameters(Map.of("USERNAME", username, "PASSWORD", password))
//                .build());
//    }
//
//    public void globalSignOut(String username) {
//        client.adminUserGlobalSignOut(AdminUserGlobalSignOutRequest.builder()
//                .userPoolId(props.getUserPoolId())
//                .username(username)
//                .build());
//    }
//
    public void deleteUser(String username) {
        client.adminDeleteUser(AdminDeleteUserRequest.builder()
                .userPoolId(props.getUserPoolId())
                .username(username)
                .build());
    }
}