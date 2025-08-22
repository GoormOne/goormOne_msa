package com.example.storeservice.service;

import com.example.storeservice.entity.AiDocumentEntity;
import com.example.storeservice.repository.AiDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiDocumentService {

    private final AiDocumentRepository repository;

    public AiDocumentEntity save(AiDocumentEntity entity) {
        return repository.save(entity);
    }

    public Optional<AiDocumentEntity> findById(UUID id) {
        return repository.findById(id);
    }
}
