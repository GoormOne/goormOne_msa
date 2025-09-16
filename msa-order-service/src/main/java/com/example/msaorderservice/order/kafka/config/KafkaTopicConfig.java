package com.example.msaorderservice.order.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile({"local","dev"})
public class KafkaTopicConfig {

	@Bean
	public NewTopic orderEvents(@Value("${topics.order.events}") String name) {
		return TopicBuilder.name(name)
			.partitions(3)
			.replicas(1)
			.config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7 * 24 * 60 * 60 * 1000L)) // 7d
			.build();
	}

	@Bean
	public NewTopic paymentCommandTopic(@Value("${topics.payment.commands}") String name) {
		return TopicBuilder.name(name)
			.partitions(3)
			.replicas(1)
			.config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7 * 24 * 60 * 60 * 1000L)) // 7d
			.build();
	}

	@Bean
	public NewTopic paymentEventTopic(@Value("${topics.payment.events}") String name) {
		return TopicBuilder.name(name)
			.partitions(3)
			.replicas(1)
			.config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7 * 24 * 60 * 60 * 1000L)) // 7d
			.build();
	}

	// 공통 DLT
	@Bean
	public NewTopic sagaDlt(@Value("${topics.saga.dlt}") String name){
		return TopicBuilder.name(name)
			.partitions(3)
			.replicas(1)
			.config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7 * 24 * 60 * 60 * 1000L))
			.build();

	}
}
