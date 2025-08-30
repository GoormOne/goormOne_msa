package com.example.storeservice.controller;


import com.example.common.dto.ApiResponse;
import com.example.common.exception.CommonCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/stores/test")
@Slf4j
public class TestController {
    private final Job jobReview;            // reviewsDenormJob 주입될 자리
    private final JobLauncher jobLauncher;

    public TestController(
            @Qualifier("reviewsDenormJob") Job job,
            JobLauncher jobLauncher
    ) {
        this.jobReview = job;
        this.jobLauncher = jobLauncher;
    }


    @GetMapping("/batch")
    public ResponseEntity<ApiResponse<?>> testBatch() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestId", UUID.randomUUID().toString())
                .addLong("ts", System.currentTimeMillis()) // 유니크 보장
                .toJobParameters();

        try {
            JobExecution exec = jobLauncher.run(jobReview, jobParameters); // 동기: 여기서 완료까지 대기
            return ResponseEntity.ok(
                    ApiResponse.success(Map.of(
                            "jobName", exec.getJobInstance().getJobName(),
                            "executionId", exec.getId(),
                            "status", exec.getStatus().toString(),          // COMPLETED/FAILED
                            "exitCode", exec.getExitStatus().getExitCode(), // COMPLETED/FAILED
                            "startTime", Objects.requireNonNull(exec.getStartTime()),
                            "endTime", Objects.requireNonNull(exec.getEndTime())
                    ))
            );
        } catch (JobExecutionAlreadyRunningException e) {
            log.warn("Job is already running", e);
            return ResponseEntity.status(409)
                    .body(ApiResponse.fail(CommonCode.BAD_REQUEST, e.getMessage()));
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("Job instance already complete", e);
            return ResponseEntity.status(409)
                    .body(ApiResponse.fail(CommonCode.BAD_REQUEST, e.getMessage()));
        } catch (JobRestartException | JobParametersInvalidException e) {
            log.error("Failed to start job", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(CommonCode.BAD_REQUEST, e.getMessage()));
        }
    }
}
