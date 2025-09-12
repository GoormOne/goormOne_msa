package com.example.storeservice.chat;

import lombok.*;

import java.util.UUID;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class ChatResponseMsg {
    private UUID requestId;
    private String answer;
}
