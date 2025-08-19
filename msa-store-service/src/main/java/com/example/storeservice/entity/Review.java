package com.example.storeservice.entity;

import com.example.storeservice.dto.CreateReviewDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @Column(name = "review_id", nullable = false, updatable = false)
    private UUID reviewId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reviews_store"))
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment")
    private String comment;

    @Builder.Default
    @Column(name = "is_public", insertable = false)
    private Boolean isPublic = Boolean.TRUE;

    @Column(name = "is_deleted", insertable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;


    public Review(CreateReviewDto createReviewDto, UUID storeId) {
        this.reviewId = UUID.randomUUID();
        this.customerId = createReviewDto.getCustomerId();
        this.menu = new Menu(createReviewDto.getMenuId());
        this.comment = createReviewDto.getComment();
        this.store = new Store(storeId);
        this.rating = 0;
        this.isPublic = true;
    }

}