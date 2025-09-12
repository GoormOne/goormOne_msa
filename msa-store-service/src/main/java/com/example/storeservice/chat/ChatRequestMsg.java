package com.example.storeservice.chat;

import lombok.*;

import java.util.UUID;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class ChatRequestMsg {
    private UUID requestId;
    private UUID storeId;
    private UUID menuId;
    private String query;
}
