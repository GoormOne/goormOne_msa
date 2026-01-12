package com.example.storeservice.mongoDB;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "reviews")  // MongoDB collection 이름
@ToString
public class AiDocumentEntity {

    @Id
    private String id;

    @Field("store_name")
    private String storeName;

    @Field("updated_at")
    private LocalDateTime updateAt;

    @Builder.Default
    private List<Menus> menus = List.of();  // 빌더가 null 안 넣고 기본값 유지 => null값 방지용


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
        private List<Reviews> reviews = List.of();

    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Reviews{
        @Field("review_id")
        private String reviewId;
        @Field("text")
        private String text;
        @Field("created_at")
        private LocalDateTime createdAt;

    }

}
