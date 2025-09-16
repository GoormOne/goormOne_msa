package com.example.msapaymentservice.config;
//
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;
// import org.springframework.kafka.core.ProducerFactory;
// import org.springframework.kafka.transaction.KafkaTransactionManager;
// import org.springframework.orm.jpa.JpaTransactionManager;
// import org.springframework.transaction.PlatformTransactionManager;
// import org.springframework.transaction.annotation.EnableTransactionManagement;
//
// import jakarta.persistence.EntityManagerFactory;
//
// @Configuration
// @EnableTransactionManagement
// public class TransactionConfig {
//
// 	@Bean
// 	@Primary
// 	public PlatformTransactionManager jpaTransactionManger(EntityManagerFactory emf) {
// 		return new JpaTransactionManager(emf);
// 	}
//
// 	@Bean(name = "kafkaTxManager")
// 	public KafkaTransactionManager<String, String> kafkaTransactionManager(
// 		ProducerFactory<String, String> producerFactory) {
// 		return new KafkaTransactionManager<>(producerFactory);
// 	}
// }
