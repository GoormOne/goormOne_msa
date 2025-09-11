package com.example.msaorderservice;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// Topic을 생성하는 설정 파일
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic testTopic1() {
        return TopicBuilder.name("test-topic-1")
                .partitions(3)                            // 파티션 수 설정
                .replicas(1)                               // 복제 팩터 설정
                .config(
                        TopicConfig.RETENTION_MS_CONFIG,
                        String.valueOf(7 * 24 * 60 * 60 * 1000L)    // 7일
                )
                .build();
    }

    @Bean
    public NewTopic testTopic2() {
        return TopicBuilder.name("test-topic-2")
                .partitions(1)
                .replicas(1)
                .compact()                                             // 압축 정책 설정
                .build();
    }
}
