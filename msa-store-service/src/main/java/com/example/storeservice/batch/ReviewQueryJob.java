package com.example.storeservice.batch;


import com.example.storeservice.mongoDB.ReviewQueryMongoRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ReviewQueryJob {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ReviewQueryMongoRepository reviewQueryRepository;

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
