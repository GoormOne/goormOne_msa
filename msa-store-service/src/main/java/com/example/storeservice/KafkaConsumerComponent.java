package com.example.storeservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Kafka Producer로부터 생성된 Topic 내의 메시지 수신
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerComponent {

    private final KafkaConsumerService kafkaConsumerService;

    @KafkaListener(topics = "test-topic-1")
    public void listen(String message) {
        try {
            log.info("메시지 수신: {}", message);
            String result = kafkaConsumerService.processMessage(message);
            log.info("메시지 처리 완료: {}", result);
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
