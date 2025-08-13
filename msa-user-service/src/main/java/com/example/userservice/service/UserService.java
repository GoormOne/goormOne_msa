package com.example.userservice.service;

import com.example.userservice.dto.request.SignupRequestDto;
import com.example.userservice.entity.UserAudit;
import com.example.userservice.entity.Owner;
import com.example.userservice.entity.User;
import com.example.userservice.repository.OwnerRepository;
import com.example.userservice.repository.UserAuditRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserAuditRepository userAuditRepository;
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UUID signup(SignupRequestDto dto) {
        // 1) 중복 체크
        userRepository.findByUsername(dto.getUsername())
                .ifPresent(u -> { throw new IllegalArgumentException("USERNAME_DUPLICATED"); });
        userRepository.findByEmail(dto.getEmail())
                .ifPresent(u -> { throw new IllegalArgumentException("EMAIL_DUPLICATED"); });

        // 2) 유저 저장 (DB가 UUID 생성)
        User user = dto.toEntity();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);    // 여기서 saved.getId() 확정

        // 3) 감사 저장 (PK=FK: audit_id = user_id, created_by = user_id)
        UserAudit audit = UserAudit.builder()
                .auditId(saved.getUserId())
                .createdAt(LocalDateTime.now())
                .createdBy(saved.getUserId())
                .build();
        userAuditRepository.save(audit);

        log.info("회원가입 완료 userId={}", saved.getUserId());
        return saved.getUserId();
    }

}

// 기존 코드 주석처리
/*
import com.profect.delivery.domain.users.dto.UserInfoDto;
import com.profect.delivery.domain.users.dto.request.LoginRequestDto;
import com.profect.delivery.domain.users.dto.request.SignupRequestDto;
import com.profect.delivery.domain.users.dto.request.UserUpdateRequestDto;
import com.profect.delivery.domain.users.dto.response.UserResponseDto;
import com.profect.delivery.global.auth.jwt.JwtTokenProvider;
import com.profect.delivery.global.exception.BusinessException;
import com.profect.delivery.global.exception.custom.AuthErrorCode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

private final JwtTokenProvider jwtTokenProvider;

@Transactional(readOnly = true)
public UserResponseDto getUserById(String userId) {
    User user = userRepository.findByUserId(userId);
    if (user == null) {
        throw new BusinessException(AuthErrorCode.NOT_FOUND_USER);
    }
    return UserResponseDto.fromEntity(user);
}

@Transactional
public void updateUser(UserUpdateRequestDto userUpdateRequestDto, String userId, String updatedBy) {
    User user = userRepository.findByUserId(userId);
    if (user == null) {
        throw new BusinessException(AuthErrorCode.NOT_FOUND_USER);
    }
    
    user.update(
        userUpdateRequestDto.getName(),
        userUpdateRequestDto.getPassword(),
        userUpdateRequestDto.getEmail(),
        userUpdateRequestDto.getIs_public(),
        updatedBy
    );
    userRepository.save(user);
    log.info("Updated user id: {}", user.getUserId());
}

@Transactional(readOnly = true)
public User findByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new BusinessException(AuthErrorCode.NOT_FOUND_USER));
}

@Transactional
public String createUser(SignupRequestDto request) {
    if (userRepository.findByUsername(request.username()).isPresent()) {
        throw new BusinessException(AuthErrorCode.DUPLICATE_USERNAME);
    }
    if (userRepository.findByEmail(request.email()).isPresent()) {
        throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);
    }
    
    User user = new User();
    user.setUserId(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10));
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setName(request.name());
    user.setBirth(java.sql.Date.valueOf(request.birth()));
    user.setEmail(request.email());
    user.setRole(request.role());
    user.setPublic(true);
    user.setBanned(false);
    user.setCreatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
    user.setCreatedBy("SYSTEM");
    
    userRepository.save(user);
    log.info("Created new user: {}", user.getUsername());
    return "User created successfully";
}

@Transactional
public String softDeleteUser(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new BusinessException(AuthErrorCode.NOT_FOUND_USER));
    
    user.setDeletedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
    user.setDeletedBy(username);
    
    userRepository.save(user);
    log.info("Soft deleted user: {}", username);
    return "User withdrawn successfully";
}

@Transactional
public TokenInfo login(LoginRequestDto request) {
    User user = findByUsername(request.username());
    
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
        throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
    }

    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            user.getUsername(), null, 
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    
    TokenInfo tokenInfo = jwtTokenProvider.generateToken(authToken);
    log.info("Login successful for user: {}", user.getUsername());
    return tokenInfo;
}

@Transactional(readOnly = true)
public UserInfoDto getCurrentUser(String username) {
    User user = findByUsername(username);
    return new UserInfoDto(user.getUsername(), user.getRole(), user.getName(), user.getEmail());
}
*/