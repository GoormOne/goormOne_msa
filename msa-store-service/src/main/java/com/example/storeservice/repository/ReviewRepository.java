package com.example.storeservice.repository;


import com.example.storeservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByReviewIdAndIsDeletedFalse(UUID reviewId);
    Page<Review> findByStore_StoreIdAndIsDeletedFalse(UUID storeId, Pageable pageable);

    List<Review> findByMenu_MenuId(UUID menuMenuId);
}
