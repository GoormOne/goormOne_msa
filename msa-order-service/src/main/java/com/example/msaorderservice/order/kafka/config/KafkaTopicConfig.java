package com.example.msaorderservice.order.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

	@Bean
	public NewTopic orderEvents() {
		return TopicBuilder.name("order-events")
			.partitions(3)
			.replicas(1)
			.config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7 * 24 * 60 * 60 * 1000L)) // 7d
			.build();
	}

	// 공통 DLT
	@Bean
	public NewTopic sagaDlt(){
		return TopicBuilder.name("saga-dlt")
			.partitions(3)
			.replicas(1)
			.config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7 * 24 * 60 * 60 * 1000L))
			.build();

	}
}
