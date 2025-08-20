package com.example.storeservice.repository;


import com.example.storeservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends CrudRepository<Review, UUID> {
    Optional<Review> findByReviewIdAndIsDeletedFalse(UUID reviewId);
    Page<Review> findByStore_StoreIdAndIsDeletedFalse(UUID storeId, Pageable pageable);

}
