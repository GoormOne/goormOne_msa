package com.example.userservice.service;

import com.example.userservice.dto.CustomerProfileRes;
import com.example.userservice.dto.CustomerProfileUpdateReq;
import com.example.userservice.dto.OwnerProfileRes;
import com.example.userservice.dto.OwnerProfileUpdateReq;
import com.example.userservice.exception.ServiceException;
import com.example.userservice.repository.CustomerRepository;
import com.example.userservice.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final CustomerRepository customerRepository;
    private final OwnerRepository ownerRepository;

    // ===== customers =====
    @Transactional(readOnly = true)
    public CustomerProfileRes getCustomer(UUID customerId) {
        var c = customerRepository.findByCustomerId(customerId).orElseThrow(ServiceException::notFound);
        return toCustomerRes(c.getCustomerId(), c.getUsername(), c.getName(), c.getEmail(), c.getIsBanned(), c.getEmailVerified());
    }

    @Transactional
    public CustomerProfileRes updateCustomer(UUID customerId, CustomerProfileUpdateReq req) {
        var c = customerRepository.findByCustomerId(customerId).orElseThrow(ServiceException::notFound);
        if (customerRepository.existsByEmailAndCustomerIdNot(req.getEmail(), customerId)) throw ServiceException.duplicatedEmail();

        c.setName(req.getName());
        c.setEmail(req.getEmail());
        c.setUpdatedBy(customerId);  // 감사 주체 기록
        return toCustomerRes(c.getCustomerId(), c.getUsername(), c.getName(), c.getEmail(), c.getIsBanned(), c.getEmailVerified());
    }

    @Transactional
    public void deleteCustomer(UUID customerId, String reason) {
        var c = customerRepository.findByCustomerId(customerId).orElseThrow(ServiceException::notFound);
        c.setDeletedBy(customerId);
        c.setDeletedRs(reason);
        // @PreRemove 대신 soft delete -> deleted_at/by/rs 세팅 (AuditBaseEntity에 로직이 있으면 따라감)
    }

    // ===== owners =====
    @Transactional(readOnly = true)
    public OwnerProfileRes getOwner(UUID ownerId) {
        var o = ownerRepository.findByOwnerId(ownerId).orElseThrow(ServiceException::notFound);
        return toOwnerRes(o.getOwnerId(), o.getUsername(), o.getName(), o.getEmail(), o.getIsBanned(), o.getEmailVerified());
    }

    @Transactional
    public OwnerProfileRes updateOwner(UUID ownerId, OwnerProfileUpdateReq req) {
        var o = ownerRepository.findByOwnerId(ownerId).orElseThrow(ServiceException::notFound);
        if (ownerRepository.existsByEmailAndOwnerIdNot(req.getEmail(), ownerId)) throw ServiceException.duplicatedEmail();

        o.setName(req.getName());
        o.setEmail(req.getEmail());
        o.setUpdatedBy(ownerId);
        return toOwnerRes(o.getOwnerId(), o.getUsername(), o.getName(), o.getEmail(), o.getIsBanned(), o.getEmailVerified());
    }

    @Transactional
    public void deleteOwner(UUID userId, String reason) {
        var o = ownerRepository.findByOwnerId(userId).orElseThrow(ServiceException::notFound);
        o.setDeletedBy(userId);
        o.setDeletedRs(reason);
    }

    private CustomerProfileRes toCustomerRes(UUID id, String username, String name, String email, Boolean banned, Boolean emailVerified) {
        return new CustomerProfileRes(id, username, name, email, banned, emailVerified);
    }
    private OwnerProfileRes toOwnerRes(UUID id, String username, String name, String email, Boolean banned, Boolean emailVerified) {
        return new OwnerProfileRes(id, username, name, email, banned, emailVerified);
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