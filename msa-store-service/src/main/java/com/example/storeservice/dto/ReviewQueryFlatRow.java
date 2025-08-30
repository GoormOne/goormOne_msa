package com.example.storeservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ReviewQueryFlatRow {

    private UUID storeId;
    private String storeName;
    private UUID menuId;
    private String menuName;
    private UUID requestId;
    private String question;
    private LocalDateTime createdAt;

}
