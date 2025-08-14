package com.example.userservice.service;

import com.example.userservice.entity.CustomerAudit;
import com.example.userservice.repository.UserAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAuditService {
    private final UserAuditRepository userAuditRepository;

    public CustomerAudit getAudit(UUID auditId) {
        return userAuditRepository.findById(auditId).orElse(null);
    }
}
