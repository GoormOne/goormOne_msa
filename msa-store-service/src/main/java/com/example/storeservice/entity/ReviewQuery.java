package com.example.storeservice.entity;

import com.example.storeservice.dto.ReviewQueryDto;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_review_queries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "question_id", nullable = false, updatable = false)
    private UUID questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "question_text", nullable = false, columnDefinition = "text")
    private String questionText;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

//    public  ReviewQuery(ReviewQueryDto reviewQueryDto,  UUID customerId) {
//        this.menu = new Menu(reviewQueryDto.getMenuId());
//        this.customerId = customerId;
//        this.questionText = reviewQueryDto.getQuery();
//        this.createdAt = LocalDateTime.now();
//        this.deleted = false;
//    }
}
