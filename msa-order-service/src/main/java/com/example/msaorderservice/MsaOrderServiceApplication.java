package com.example.msaorderservice;

import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableCaching
@SpringBootApplication(scanBasePackages = "com.example")
public class MsaOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsaOrderServiceApplication.class, args);
	}

}
