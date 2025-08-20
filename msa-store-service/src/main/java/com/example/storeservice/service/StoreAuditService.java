package com.example.storeservice.service;

import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.exception.StoreAlreadyDeletedException;
import com.example.storeservice.repository.StoreAuditRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreAuditService {
    private final StoreAuditRepository storeAuditRepository;

    public StoreAudit insertStoreAudit(StoreAudit storeAudit) {
        return storeAuditRepository.save(storeAudit);
    }

    public StoreAudit getAudit(UUID auditId) {
        return storeAuditRepository.findById(auditId).orElseThrow(
                () -> new EntityNotFoundException("감사내역이 없습니다 : " + auditId)
        );
    }

    @Transactional
    public void updateAudit(UUID auditId,  UUID byId) {
        StoreAudit storeAudit = storeAuditRepository.findById(auditId).orElseThrow(
                () -> new EntityNotFoundException("감사내역이 없습니다 : " + auditId)
        );

        storeAudit.setUpdatedAt(LocalDateTime.now());
        storeAudit.setUpdatedBy(byId);
    }

    @Transactional
    public StoreAudit deleteAudit(UUID auditId, UUID deleterId) {
        StoreAudit storeAudit = getAudit(auditId);
        if (storeAudit.getDeletedAt() != null) {
            throw new StoreAlreadyDeletedException("이미 삭제된 상점입니다. ");
        }
        //TODO -- 검색이 안될 시

        storeAudit.setDeletedBy(deleterId);
        storeAudit.setDeletedAt(LocalDateTime.now());
        return storeAudit;
    }


}
