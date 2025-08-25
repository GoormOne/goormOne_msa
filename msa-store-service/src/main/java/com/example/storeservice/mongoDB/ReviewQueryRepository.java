package com.example.storeservice.mongoDB;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewQueryRepository extends MongoRepository<ReviewQueryEntity, String> {
}
