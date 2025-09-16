package com.example.storeservice.stock.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

// 모든 이벤트 공통 부모 (빌더 상속 문제 해결을 위해 @SuperBuilder 사용)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public abstract class BaseEvent {
    private String  type;       // payload type
    private UUID    eventId;    // idempotency key
    private UUID    orderId;
    private UUID    storeId;
    private Instant occurredAt;
}
