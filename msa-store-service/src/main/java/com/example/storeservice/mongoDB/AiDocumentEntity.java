package com.example.storeservice.mongoDB;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "reviews_denorm")  // MongoDB collection 이름
@ToString
public class AiDocumentEntity {

    @Id
    private UUID storeId;
    private String storeName;
    @Builder.Default
    private List<Menus> menus = List.of();  // 빌더가 null 안 넣고 기본값 유지 => null값 방지용
    private LocalDateTime updateAt;

    public AiDocumentEntity(UUID storeId, String storeName, LocalDateTime updateAt) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.updateAt = updateAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Menus{
        private UUID menuId;
        private String menuName;
        @Builder.Default
        private List<Reviews> reviews = List.of();

        public Menus(UUID menuId, String menuName) {
            this.menuId = menuId;
            this.menuName = menuName;
        }

    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Reviews{
        private UUID reviewId;
        private String text;
        private LocalDateTime createAt;

    }
}
