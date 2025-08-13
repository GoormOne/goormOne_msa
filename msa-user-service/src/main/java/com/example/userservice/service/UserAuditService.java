package com.example.userservice.service;

import com.example.userservice.entity.UserAudit;
import com.example.userservice.repository.UserAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAuditService {
    private final UserAuditRepository userAuditRepository;

    public UserAudit getAudit(UUID auditId) {
        return userAuditRepository.findById(auditId).orElse(null);
    }
}
