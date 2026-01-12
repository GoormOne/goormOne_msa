package com.example.storeservice.stock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 도메인 단위 토픽 이름 보관.
 * - inbound: order-events
 * - outbound: stock-events
 */
@Slf4j
@Getter
@Component
public class TopicRegistry {

    @Value("${topics.order.events:order-events}")
    private String orderEvents;

    @Value("${topics.stock.events:stock-events}")
    private String stockEvents;

    public TopicRegistry() {}
}
