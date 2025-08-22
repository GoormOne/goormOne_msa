// package com.example.storeservice.controller;
//
//
// import com.example.common.dto.ApiResponse;
// import com.example.storeservice.dto.CreateReviewDto;
// import com.example.storeservice.dto.ReviewDto;
// import com.example.storeservice.entity.Review;
// import com.example.storeservice.interceptor.RequireStoreOwner;
// import com.example.storeservice.service.ReviewService;
// import jakarta.validation.constraints.Max;
// import jakarta.validation.constraints.Min;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
//
// import java.util.List;
// import java.util.UUID;
//
// @RestController
// @RequestMapping("/stores/{storeId}/reviews")
// @Slf4j
// @RequiredArgsConstructor
// public class ReviewController {
//
//     private final ReviewService reviewService;
//
//     @GetMapping("/{reviewId}")
//     public ResponseEntity<ApiResponse<?>> getReview(@PathVariable UUID reviewId) {
//         Review review = reviewService.getById(reviewId);
//         return ResponseEntity.ok(ApiResponse.success(ReviewDto.from(review)));
//     }
//     @GetMapping("/")
//     public ResponseEntity<ApiResponse<?>> getReviews(
//             @PathVariable UUID storeId,
//             @RequestParam(defaultValue = "1") @Min(1) int page,  // 1부터 받기
//             @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
//             @RequestParam(defaultValue = "createdAt,desc") String sort
//         ) {
//         int page0 = page - 1;
//         Sort s = Sort.by(
//                 sort.split(",").length == 2 && "asc".equalsIgnoreCase(sort.split(",")[1])
//                         ? Sort.Order.asc(sort.split(",")[0])
//                         : Sort.Order.desc(sort.split(",")[0])
//                 );
//         Pageable pageable = PageRequest.of(page0, size, s);
//
//         Page<Review> reviewPage = reviewService.getByStoreId(storeId, pageable);
//         List<ReviewDto> reviewDtoList = reviewPage.getContent()
//                 .stream()
//                 .map(ReviewDto::from)
//                 .toList();
//
//         return ResponseEntity.ok(ApiResponse.success(reviewDtoList));
//     }
//
//     //todo 메뉴기준 reviews
//     @GetMapping("/menu/{menuId}")
//     public ResponseEntity<ApiResponse<?>> getReviewsByMenuId(
//             @PathVariable UUID storeId,
//             @PathVariable UUID menuId
//     ){
//
//
//         return ResponseEntity.ok(ApiResponse.success());
//     }
//
//     @PostMapping("/")
//     public ResponseEntity<ApiResponse<?>> postReview(
//             @PathVariable UUID storeId,
//             @RequestBody CreateReviewDto reviewDto
//     ){
//
//         //TODO : order_item 테이블에서 요청 userId와 menuId가 있는 지 확인 -> 구매내역있는 사람만 리뷰가능
//         Review review = reviewService.saveReview(new Review(reviewDto, storeId));
//
//
//         return ResponseEntity.ok(ApiResponse.success(ReviewDto.from(review)));
//     }
//
//     // todo : 리뷰 좋아요/취소
//
//
// }
