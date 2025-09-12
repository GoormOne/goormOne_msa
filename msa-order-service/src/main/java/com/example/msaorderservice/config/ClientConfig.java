package com.example.msaorderservice.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate(RestTemplateBuilder builder){
		return builder
			.setConnectTimeout(Duration.ofMillis(500))
			.setReadTimeout(Duration.ofMillis(3000))
			.build();
	}
}
