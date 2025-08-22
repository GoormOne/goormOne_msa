package com.example.storeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiDocumentDto {

    private UUID storeId;
    private String storeName;
    @Builder.Default
    private List<Menus> menus = List.of();  // 빌더가 null 안 넣고 기본값 유지 => null값 방지용
    private LocalTime updateAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Menus{
        private UUID menuId;
        private String menuName;
        @Builder.Default
        private List<Reviews> reviews = List.of();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Reviews{
        private UUID reviewId;
        private String text;
        private LocalTime createAt;
    }

}
