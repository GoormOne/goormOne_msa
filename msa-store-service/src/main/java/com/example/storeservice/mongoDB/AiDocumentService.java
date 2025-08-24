package com.example.storeservice.mongoDB;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiDocumentService {

    private final AiDocumentRepository repository;

    public List<AiDocumentEntity> saveAll(List<AiDocumentEntity> entities) {
        // save는 @Id(storeId) 없으면 insert, 있으면 replace 수행함.
        return repository.saveAll(entities);
    }

    public Optional<AiDocumentEntity> findById(UUID id) {
        return repository.findById(id);
    }
}
