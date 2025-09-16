package com.example.storeservice.stock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 도메인 단위 토픽 이름을 프로퍼티에서 주입받아 보관.
 * - @KafkaListener 에서는 SpEL로 #{@topicRegistry.inboundTopics} 사용
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
