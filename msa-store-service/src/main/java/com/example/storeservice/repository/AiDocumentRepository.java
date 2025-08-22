package com.example.storeservice.repository;

import com.example.storeservice.entity.AiDocumentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AiDocumentRepository extends MongoRepository<AiDocumentEntity, UUID> {

}
