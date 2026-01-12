package com.example.storeservice.batch;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// BatchAsyncConfig.java
@Configuration
public class BatchAsyncConfig {

    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(4);
        exec.setMaxPoolSize(8);
        exec.setQueueCapacity(100);
        exec.setThreadNamePrefix("batch-");
        exec.initialize();
        return exec;
    }

    // 비동기 JobLauncher (기본 jobLauncher 와 구분되도록 이름 부여)
    @Bean("asyncJobLauncher")
    public JobLauncher asyncJobLauncher(
            JobRepository jobRepository,
            @Qualifier("batchTaskExecutor") TaskExecutor executor //Bean으로 등록된 TaskExecutor
    ) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(executor); // 비동기 실행
        launcher.afterPropertiesSet();
        return launcher;
    }
}
