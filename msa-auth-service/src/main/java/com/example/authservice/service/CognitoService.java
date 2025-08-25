package com.example.authservice.service;

import com.example.authservice.exception.CognitoCreateException;
import com.example.authservice.properties.CognitoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUserGlobalSignOutRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public void createUserAndSetPasswordAndGroup(
            String username,
            String rawPassword,
            String email,
            String name,
            LocalDate birth,     // null 허용
            String groupName     // null/blank 허용
    ) {
        createUserAndSetPasswordAndGroup(
                username,
                rawPassword,
                email,
                name,
                birth,
                groupName,
                null,      // custom attrs
                true       // email_verified
        );
    }

    /**
     * 확장형(옵션): 커스텀 속성까지 주입해야 하는 경우 사용
     * - 예: Map.of("custom:user_id", customerId.toString())
     * - emailVerified 정책도 호출부에서 제어 가능
     */
    public void createUserAndSetPasswordAndGroup(
            String username,
            String rawPassword,
            String email,
            String name,
            LocalDate birth,                       // null 허용
            String groupName,                      // null/blank 허용
            Map<String, String> customAttrsToSet,  // null 허용
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

            // 4) 그룹 매핑
            if (groupName != null && !groupName.isBlank()) {
                AdminAddUserToGroupRequest addReq = AdminAddUserToGroupRequest.builder()
                        .userPoolId(props.getUserPoolId())
                        .username(username)
                        .groupName(groupName)
                        .build();
                client.adminAddUserToGroup(addReq);
                log.info("[COGNITO] AdminAddUserToGroup OK username={}, group={}", username, groupName);
            }

        } catch (UsernameExistsException e) {
            throw new CognitoCreateException(HttpStatus.CONFLICT, "USERNAME_EXISTS");
        } catch (InvalidPasswordException e) {
            throw new CognitoCreateException(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_POLICY");
        } catch (ResourceNotFoundException e) {
            throw new CognitoCreateException(HttpStatus.NOT_FOUND, "USERPOOL_OR_GROUP_NOT_FOUND");
        } catch (CognitoIdentityProviderException e) {
            throw new CognitoCreateException(HttpStatus.BAD_REQUEST, "AWS_COGNITO_ERROR: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            throw new CognitoCreateException(HttpStatus.BAD_REQUEST, "UNKNOWN_ERROR: " + e.getMessage());
        }
    }

    // ===== 운영/테스트용 API 유지 =====

    public AdminInitiateAuthResponse loginByAdminNoSrp(String username, String password) {
        log.info("[COGNITO] login start username={}", username);
        return client.adminInitiateAuth(AdminInitiateAuthRequest.builder()
                .userPoolId(props.getUserPoolId())
                .clientId(props.getAppClientId())
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(Map.of("USERNAME", username, "PASSWORD", password))
                .build());
    }

    public AdminInitiateAuthResponse login(String username, String password) {
        log.info("[COGNITO] login start username={}", username);
        return client.adminInitiateAuth(AdminInitiateAuthRequest.builder()
                .userPoolId(props.getUserPoolId())
                .clientId(props.getAppClientId())
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(Map.of("USERNAME", username, "PASSWORD", password))
                .build());
    }

    public void globalSignOut(String username) {
        client.adminUserGlobalSignOut(AdminUserGlobalSignOutRequest.builder()
                .userPoolId(props.getUserPoolId())
                .username(username)
                .build());
    }

    public void deleteUser(String username) {
        client.adminDeleteUser(AdminDeleteUserRequest.builder()
                .userPoolId(props.getUserPoolId())
                .username(username)
                .build());
    }
}