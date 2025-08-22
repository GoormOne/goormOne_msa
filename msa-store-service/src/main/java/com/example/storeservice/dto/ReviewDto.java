package com.example.storeservice.dto;


import com.example.storeservice.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private UUID customerId;
    private UUID storeId;
    private UUID menuId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private boolean isPublic = true;


    public static ReviewDto from(Review review) {
        if (review == null) return null;
        return new ReviewDto(
                review.getCustomerId(),
                review.getStore().getStoreId(),
                review.getMenu().getMenuId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getIsPublic()
        );
    }
}
