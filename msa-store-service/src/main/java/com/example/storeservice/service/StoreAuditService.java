package com.example.storeservice.service;

import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.repository.StoreAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreAuditService {
    private final StoreAuditRepository storeAuditRepository;

    public UUID insertAudit(UUID createdBy) {
        return storeAuditRepository.insertAuditReturningId(createdBy);
    }

    public StoreAudit getAudit(UUID auditId) {
        return storeAuditRepository.findById(auditId).orElse(null);
    }


}
