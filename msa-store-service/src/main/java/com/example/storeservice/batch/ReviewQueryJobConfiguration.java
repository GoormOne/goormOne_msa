package com.example.storeservice.batch;


import com.example.storeservice.dto.ReviewQueryFlatRow;
import com.example.storeservice.mongoDB.ReviewQueryEntity;
import com.example.storeservice.mongoDB.ReviewQueryMongoRepository;
import com.example.storeservice.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReviewQueryJobConfiguration {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StoreRepository storeRepository;
    private final ReviewQueryMongoRepository reviewQueryRepository;

    @Bean
    public Job reviewQueryJob() {
        return new JobBuilder("reviewQueryJob", jobRepository)
                .start(reviewQueryStep())
                .build();
    }

    @Bean
    public Step reviewQueryStep() {
        return new StepBuilder("reviewQueryStep", jobRepository)
                .<List<ReviewQueryFlatRow>, List<ReviewQueryEntity>>chunk(1, transactionManager)
                .reader(QueryFlatRowsPageReader())
                .processor(this::queryToDocuments)
                .writer(docsChunk -> {                 // docsChunk = List<List<AiDocumentEntity>>
                    if (docsChunk.isEmpty()) return;
                    reviewQueryRepository.saveAll(docsChunk.getItems().get(0));
                })
                .build();
    }


    @Bean
    public ItemStreamReader<List<ReviewQueryFlatRow>> QueryFlatRowsPageReader() {
        return new QueryFlatRowsPageReader(storeRepository, 100);
    }

    private List<ReviewQueryEntity> queryToDocuments(List<ReviewQueryFlatRow> rows) {
        List<ReviewQueryEntity> result = new ArrayList<>();
        if (rows == null || rows.isEmpty()) return result;

        // storeId -> 완성 중인 문서
        Map<UUID, ReviewQueryEntity> storeMap = new LinkedHashMap<>();
        // storeId -> (menuId -> 메뉴)
        Map<UUID, Map<UUID, ReviewQueryEntity.Menus>> menuMapByStore = new LinkedHashMap<>();

        for (ReviewQueryFlatRow r : rows) {
            UUID storeId  = r.getStoreId();
            UUID menuId   = r.getMenuId();
            UUID requestId = r.getRequestId();

            // 1) 스토어 문서 확보/생성
            ReviewQueryEntity storeDoc = storeMap.get(storeId);
            if (storeDoc == null) {
                storeDoc = ReviewQueryEntity.builder()
                        .id(storeId.toString())
                        .storeName(r.getStoreName())
                        .menus(new ArrayList<>())
                        .updatedAt(LocalDateTime.now())
                        .build();
                storeMap.put(storeId, storeDoc);
                menuMapByStore.put(storeId, new LinkedHashMap<>());
            }

            // 2) 메뉴 확보/생성 + 스토어에 연결
            Map<UUID, ReviewQueryEntity.Menus> menusOfStore = menuMapByStore.get(storeId);
            ReviewQueryEntity.Menus menuDoc = menusOfStore.get(menuId);
            if (menuDoc == null) {
                menuDoc = ReviewQueryEntity.Menus.builder()
                        .menuId(menuId.toString())
                        .menuName(r.getMenuName())
                        .questions(new ArrayList<>())
                        .build();
                menusOfStore.put(menuId, menuDoc);
                storeDoc.getMenus().add(menuDoc);
            }

            // 3) 리뷰쿼리 연결
            ReviewQueryEntity.Questions questionsDoc = ReviewQueryEntity.Questions.builder()
                    .requestId(requestId.toString())
                    .question(r.getQuestion())
                    .createdAt(r.getCreatedAt())
                    .build();

            menuDoc.getQuestions().add(questionsDoc);
        }

        for (ReviewQueryFlatRow r : rows) {
            log.info("ezra ReviewQueryFlat {}", r.toString());
        }

        for (ReviewQueryEntity r : storeMap.values()){
            log.info("ezra storeDoc {}", r.toString());
        }

        return new ArrayList<>(storeMap.values());
    }


}
