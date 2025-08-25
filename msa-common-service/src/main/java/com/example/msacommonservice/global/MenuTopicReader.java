package com.example.msacommonservice.global;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuTopicReader {
    private final ObjectMapper om = new ObjectMapper();

    // 예: menu 토픽부터 읽기 (필요한 토픽마다 추가)
    @KafkaListener(topics = {"rds.public.p_menus"}, concurrency = "1")
    public void onMessage(ConsumerRecord<String, String> rec) throws Exception {
        String key = rec.key();
        String raw = rec.value();
        if (raw == null) {
            System.out.printf("[menu] tombstone key=%s%n", key);
            return;
        }

        // 1) Debezium unwrap 되어 있는 경우(값이 곧 실제 레코드)
        // 2) unwrap 안 된 경우(값에 {"payload":{"after":{...}}} 혹은 {"after":{...}} 형태)
        JsonNode root = om.readTree(raw);
        JsonNode record = root.has("after") ? root.get("after")
                : (root.has("payload") && root.get("payload").has("after") ? root.get("payload").get("after")
                : root); // 이미 언랩된 케이스

        // store_id 꺼내보기 (필드명은 실제 스키마에 맞춰 변경)
        String storeId = record.has("store_id") ? record.get("store_id").asText() : "(unknown)";

        System.out.printf("[menu] key=%s storeId=%s value=%s%n", key, storeId, record.toString());
    }
}
