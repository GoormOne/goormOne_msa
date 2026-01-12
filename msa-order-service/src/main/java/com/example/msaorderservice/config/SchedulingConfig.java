package com.example.msaorderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulingConfig {

	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
		ts.setPoolSize(5);
		ts.setThreadNamePrefix("order-timeout-");
		ts.initialize();
		return ts;
	}
}
