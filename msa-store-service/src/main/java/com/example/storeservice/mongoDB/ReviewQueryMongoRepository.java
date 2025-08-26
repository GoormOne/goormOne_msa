package com.example.storeservice.mongoDB;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewQueryMongoRepository extends MongoRepository<ReviewQueryEntity, String> {
}
