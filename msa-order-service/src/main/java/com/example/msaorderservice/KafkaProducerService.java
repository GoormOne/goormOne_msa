package com.example.msaorderservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// kafkaTemplate 활용하여 Topic 내에 메시지 전송
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private String topicName = "test-topic-1";

    // 기본 메시지 전송
    public void sendMessage(String message) {
        kafkaTemplate.send(topicName, message);
    }

    // 키와 함께 메시지 전송
    public void sendMessageWithKey(String key, String message) {
        kafkaTemplate.send(topicName, key, message);
    }

    // 특정 파티션으로 메시지 전송
    public void sendMessageToPartition(String message, int partition) {
        kafkaTemplate.send(topicName, partition, null, message);
    }

    // 비동기 전송 결과 처리
    public void sendMessageWithCallback(String message) {
        kafkaTemplate.send(topicName, message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Success: {}", result.getRecordMetadata());
                    } else {
                        log.error("Error: {}", ex.getMessage());
                    }
                });
    }
}
