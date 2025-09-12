package com.example.storeservice.service;

import com.example.storeservice.entity.Review;
import com.example.storeservice.entity.ReviewQuery;
import com.example.storeservice.repository.ReviewQueryRepository;
import com.example.storeservice.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;

    public Review getById(UUID id) {
        return reviewRepository.findByReviewIdAndIsDeletedFalse(id)
                .orElseThrow(()-> new EntityNotFoundException("Review not found"));
    }

    public Page<Review> getByStoreId(UUID storeId, Pageable pageable){
        Page<Review> reviewPage = reviewRepository.findByStore_StoreIdAndIsDeletedFalse(storeId, pageable);

        if (reviewPage.isEmpty()){
            throw new EntityNotFoundException("Review not found");
        }

        return reviewPage;
    }

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    public ReviewQuery saveReviewQuery(ReviewQuery reviewQuery) {
        List<Review> review =reviewRepository.findByMenu_MenuId(reviewQuery.getMenu().getMenuId());
        if (review.isEmpty()){
            throw new EntityNotFoundException("Review not found");
        }
        return reviewQueryRepository.save(reviewQuery);
    }

    @Transactional
    public void updateAnswer(UUID questionId, String answer) {
        ReviewQuery rq = reviewQueryRepository.findById(questionId)
                .orElseThrow(()-> new EntityNotFoundException("No ReviewQuery with id " + questionId));
        rq.setAnswerText(answer);
    }

}
