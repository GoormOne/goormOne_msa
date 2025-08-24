package com.example.storeservice.mongoDB;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AiDocumentRepository extends MongoRepository<AiDocumentEntity, UUID> {

}
