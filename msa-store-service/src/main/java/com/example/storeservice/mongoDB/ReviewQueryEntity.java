package com.example.storeservice.mongoDB;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "qa_queries")  // MongoDB collection 이름
@ToString
public class ReviewQueryEntity {

    @Id
    private String id;

    @Field("store_name")
    private String storeName;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<ReviewQueryEntity.Menus> menus = List.of();  // 빌더가 null 안 넣고 기본값 유지 => null값 방지용

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Menus{
        @Field("menu_id")
        private String menuId;
        @Field("menu_name")
        private String menuName;
        @Builder.Default
        private List<ReviewQueryEntity.Questions> questions = List.of();

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Questions {
        @Field("request_id")
        private String requestId;
        @Field("question")
        private String question;
        @Field("created_at")
        private LocalDateTime createdAt;
    }

}
