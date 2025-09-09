package com.example.storeservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// Kafka Listener 통해 전달 받은 메시지를 처리
@Slf4j
@Service
public class KafkaConsumerService {

    public String processMessage(String message) {
        try {
            return message.toUpperCase();
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("메시지 처리 실패", e);
        }
    }
}
