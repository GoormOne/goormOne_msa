package com.example.authservice.service;

import com.example.authservice.properties.CognitoProperties;
import com.example.authservice.dto.*;
import com.example.authservice.entity.customer.Customer;
import com.example.authservice.entity.customer.CustomerAudit;
import com.example.authservice.entity.owner.Owner;
import com.example.authservice.entity.owner.OwnerAudit;
import com.example.authservice.repository.CustomerAuditRepository;
import com.example.authservice.repository.CustomerRepository;
import com.example.authservice.repository.OwnerAuditRepository;
import com.example.authservice.repository.OwnerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final CustomerRepository customerRepository;
    private final OwnerRepository ownerRepository;
    private final CustomerAuditRepository customerAuditRepository;
    private final OwnerAuditRepository ownerAuditRepository;
    private final PasswordEncoder passwordEncoder;
    private final CognitoService cognitoService;
    private final JdbcTemplate jdbcTemplate;
    private final CognitoProperties props;

    // === Register (CUSTOMER) ===
    @Transactional
    public RegisterRes registerCustomer(RegisterCustomerReq req) {
        // 중복 검사
        customerRepository.findByUsername(req.getUsername())
                .ifPresent(x -> { throw new IllegalArgumentException("DUPLICATE_USERNAME"); });
        customerRepository.findByEmail(req.getEmail())
                .ifPresent(x -> { throw new IllegalArgumentException("DUPLICATE_EMAIL"); });

        // 1) Cognito 유저 생성 & sub 조회
        String sub = cognitoService.createUserAndReturnSub(
                req.getUsername(),
                req.getPassword(),
                req.getEmail(),
                req.getName(),
                req.getBirth(),
                "CUSTOMER"
        );

        UUID id = UUID.fromString(sub);

        try {
            // 2) DB 저장 (PK = sub)
            Customer customer = Customer.builder()
                    .customerId(id)
                    .username(req.getUsername())
                    .password(passwordEncoder.encode(req.getPassword()))
                    .name(req.getName())
                    .birth(req.getBirth())
                    .email(req.getEmail())
                    .emailVerified(true)
                    .isBanned(false)
                    .build();

            CustomerAudit audit = CustomerAudit.builder().build();
            customer.attachAudit(audit);
            // 감사 필드(작성자 등) — AuditBaseEntity를 상속한 구조라면 아래와 같이 설정
            audit.setAuditId(id);
            audit.setCreatedBy(id);

            Customer saved = customerRepository.save(customer);

            return new RegisterRes(saved.getCustomerId(), saved.getUsername());

        } catch (RuntimeException e) {
            // 3) 보상 트랜잭션: DB 실패 시 Cognito 롤백
            try {
                cognitoService.deleteUser(req.getUsername());
            } catch (Exception ex) {
                // 보상 실패는 로깅만
                log.error("[REGISTER_CUSTOMER] compensation deleteUser failed: {}", ex.getMessage(), ex);
            }
            throw e;
        }
    }

    // === Register (OWNER) ===
    @Transactional
    public RegisterRes registerOwner(RegisterOwnerReq req) {
        // 중복 검사
        ownerRepository.findByUsername(req.getUsername())
                .ifPresent(x -> { throw new IllegalArgumentException("DUPLICATE_USERNAME"); });
        ownerRepository.findByEmail(req.getEmail())
                .ifPresent(x -> { throw new IllegalArgumentException("DUPLICATE_EMAIL"); });

        // 1) Cognito 유저 생성 & sub 조회
        String sub = cognitoService.createUserAndReturnSub(
                req.getUsername(),
                req.getPassword(),
                req.getEmail(),
                req.getName(),
                req.getBirth(),
                "OWNER"
        );

        UUID id = UUID.fromString(sub);

        try {
            // 2) DB 저장 (PK = sub)
            Owner owner = Owner.builder()
                    .ownerId(id)
                    .username(req.getUsername())
                    .password(passwordEncoder.encode(req.getPassword()))
                    .name(req.getName())
                    .birth(req.getBirth())
                    .email(req.getEmail())
                    .emailVerified(true)
                    .isBanned(false)
                    .build();

            OwnerAudit audit = OwnerAudit.builder().build();
            owner.attachAudit(audit);
            audit.setAuditId(id);
            audit.setCreatedBy(id);

            Owner saved = ownerRepository.save(owner);

            return new RegisterRes(saved.getOwnerId(), saved.getUsername());

        } catch (RuntimeException e) {
            // 3) 보상 트랜잭션: DB 실패 시 Cognito 롤백
            try {
                cognitoService.deleteUser(req.getUsername());
            } catch (Exception ex) {
                log.error("[REGISTER_OWNER] compensation deleteUser failed: {}", ex.getMessage(), ex);
            }
            throw e;
        }
    }

    // === Login ===
    public LoginRes login(LoginReq req) {
        AuthenticationResultType ar = cognitoService.login(req.getUsername(), req.getPassword());
        String[] groups = cognitoService.extractGroups(ar.idToken());

        return LoginRes.builder()
                .idToken(ar.idToken())
                .accessToken(ar.accessToken())
                .refreshToken(ar.refreshToken())
                .expiresIn(ar.expiresIn() != null ? ar.expiresIn().longValue() : null)
                .tokenType(ar.tokenType())
                .groups(groups)
                .username(req.getUsername())
                .build();
    }

    public RefreshRes refresh(RefreshReq req) {
        var ar = cognitoService.refresh(req.getUsername(), req.getRefreshToken());
        return RefreshRes.builder()
                .idToken(ar.idToken())           // 환경에 따라 null일 수 있음
                .accessToken(ar.accessToken())
                .expiresIn(ar.expiresIn() != null ? ar.expiresIn().longValue() : null)
                .tokenType(ar.tokenType())
                .build();
    }

    public void logout(LogoutReq req) {
        cognitoService.globalSignOut(req.getUsername());
    }
//    public LoginRes loginCustomer(LoginReq req) {
//        AdminInitiateAuthResponse resp = cognitoService.login(req.getUsername(), req.getPassword());
//        var authRes = resp.authenticationResult();
//        return new LoginRes(authRes.idToken(), authRes.accessToken(), authRes.refreshToken(),
//                authRes.expiresIn(), authRes.tokenType());
//    }
//
//    public LoginRes loginOwner(LoginReq req) {
//        AdminInitiateAuthResponse resp = cognitoService.login(req.getUsername(), req.getPassword());
//        var authRes = resp.authenticationResult();
//        return new LoginRes(authRes.idToken(), authRes.accessToken(), authRes.refreshToken(),
//                authRes.expiresIn(), authRes.tokenType());
//    }

//    @Transactional
//    public void deleteMeCustomer(UUID customerId, String reason) {
//        Customer c = customerRepository.findById(customerId).orElseThrow();
//        c.setBanned(true);
//        customerRepository.save(c);
//
//        CustomerAudit audit = customerAuditRepository.findById(customerId).orElseThrow();
//        audit.setDeletedAt(OffsetDateTime.now());
//        audit.setDeletedBy(customerId);
//        audit.setDeletedRs(reason);
//        customerAuditRepository.save(audit);
//
//        cognitoService.deleteUser(c.getUsername());
//    }
//
//    @Transactional
//    public void deleteMeOwner(UUID ownerId, String reason) {
//        Owner o = ownerRepository.findById(ownerId).orElseThrow();
//        o.setBanned(true);
//        ownerRepository.save(o);
//
//        OwnerAudit audit = ownerAuditRepository.findById(ownerId).orElseThrow();
//        audit.setDeletedAt(OffsetDateTime.now());
//        audit.setDeletedBy(ownerId);
//        audit.setDeletedRs(reason);
//        ownerAuditRepository.save(audit);
//
//        cognitoService.deleteUser(o.getUsername());
//    }

//    public void logout(String username) {
//        cognitoService.globalSignOut(username);
//    }

    // 게이트웨이 내부용: username + principalType -> userId, name
    public ResolveRes resolve(String principalType, String username) {
        switch (principalType) {
            case "CUSTOMER" -> {
                Optional<Customer> c = customerRepository.findByUsername(username);
                if (c.isPresent()) return new ResolveRes(true, c.get().getCustomerId(), c.get().getName());
                return new ResolveRes(false, null, null);
            }
            case "OWNER" -> {
                Optional<Owner> o = ownerRepository.findByUsername(username);
                if (o.isPresent()) return new ResolveRes(true, o.get().getOwnerId(), o.get().getName());
                return new ResolveRes(false, null, null);
            }
            case "ADMIN" -> { // 필요 시 Admin 리포지토리 추가
                return new ResolveRes(false, null, null);
            }
            default -> { return new ResolveRes(false, null, null); }
        }
    }
}