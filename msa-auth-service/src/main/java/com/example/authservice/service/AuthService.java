package com.example.authservice.service;

import com.example.authservice.dto.CustomerRegisterRes;
import com.example.authservice.dto.RegisterReq;
import com.example.authservice.entity.CustomerAuth;
import com.example.authservice.exception.ServiceException;
import com.example.authservice.repository.CustomerAuthRepository;
import com.example.authservice.repository.OwnerAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerAuthRepository customerAuthRepository;
    private final OwnerAuthRepository ownerAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterReq registerCustomer(RegisterReq req) {

        if (customerAuthRepository.existsByUsername(req.getUsername())) throw ServiceException.duplicatedUsername();
        if (customerAuthRepository.existsByEmail(req.getEmail())) throw ServiceException.duplicatedEmail();

        var entity = CustomerAuth.builder()
                .customerId(UUID.randomUUID())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .birth(req.getBirth())
                .email(req.getEmail())
                .build();
        customerAuthRepository.save(entity);

        return new CustomerRegisterRes(entity.getCustomerId(), entity.getUsername());

    }
}
