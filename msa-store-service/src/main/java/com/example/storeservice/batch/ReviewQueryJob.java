package com.example.storeservice.batch;


import com.example.storeservice.dto.AiFlatRow;
import com.example.storeservice.entity.ReviewQuery;
import com.example.storeservice.mongoDB.AiDocumentEntity;
import com.example.storeservice.mongoDB.ReviewQueryEntity;
import com.example.storeservice.mongoDB.ReviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class ReviewQueryJob {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ReviewQueryRepository  reviewQueryRepository;

//    @Bean
//    public Job reviewQueryJob() {
//        return new JobBuilder("reviewQueryJob", jobRepository)
//                .start(reviewQueryStep())
//                .build();
//    }
//
//    @Bean
//    public Step reviewQueryStep() {
//        return new StepBuilder("reviewQueryStep", jobRepository)
//                .<List<ReviewQuery>, List<ReviewQueryEntity>>chunk(1, transactionManager)
//                .reader(flatRowsPageReader())
//                .processor(this::toDocuments)
//                .writer(docsChunk -> {                 // docsChunk = List<List<AiDocumentEntity>>
//                    if (docsChunk.isEmpty()) return;
//                    reviewQueryRepository.saveAll(docsChunk.getItems().get(0));
//                })
//                .build();
//    }




}
