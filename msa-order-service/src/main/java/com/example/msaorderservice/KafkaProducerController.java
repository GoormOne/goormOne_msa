package com.example.msaorderservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// REST API 호출 받아서 Kafka 메시지 생성하는 Producer 컨트롤러
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders/kafka")
public class KafkaProducerController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/messages1")
    public ResponseEntity<String> sendMessage(
            @RequestBody String message
    ) {
        kafkaProducerService.sendMessage(message);
        return ResponseEntity.ok("기본 메시지 전송");
    }

    @PostMapping("/messages2")
    public ResponseEntity<String> sendMessageWithKey(
            @RequestParam String key,
            @RequestBody String message
    ) {
        kafkaProducerService.sendMessageWithKey(key, message);
        return ResponseEntity.ok("키와 함께 메시지 전송");
    }

    @PostMapping("/messages3/{partition}")
    public ResponseEntity<String> sendMessageToPartition(
            @PathVariable int partition,
            @RequestBody String message
    ) {
        kafkaProducerService.sendMessageToPartition(message, partition);
        return ResponseEntity.ok("특정 파티션으로 메시지 전송");
    }

    @PostMapping("/messages4")
    public ResponseEntity<String> sendMessageWithCallback(
            @RequestBody String message
    ) {
        kafkaProducerService.sendMessageWithCallback(message);
        return ResponseEntity.ok("비동기 전송 결과 처리");
    }
}
