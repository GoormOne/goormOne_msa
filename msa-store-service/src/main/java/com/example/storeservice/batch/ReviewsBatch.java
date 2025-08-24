package com.example.storeservice.batch;

import com.example.storeservice.dto.AiFlatRow;
import com.example.storeservice.mongoDB.AiDocumentEntity;
import com.example.storeservice.mongoDB.AiDocumentRepository;
import com.example.storeservice.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
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

/**
 * 목표 : store의 리뷰들을 store단위의 하나의 document로 반정규화하여 저장
 * 청크는 store단위로 끊는게 best
 * store단위를 위해서 with 같은 native SQL은 지원이 어려워 코드 가독성이 떨어짐.
 * 2번의 읽기로 storeId 50개씩가져와서 다시 row읽어오는 패턴으로 구현
 * reader에서 storeIds, processor에서 한번더 읽고 처리, writer 에서 save
 **/
@Configuration
@RequiredArgsConstructor
public class ReviewsBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StoreRepository storeRepository;
    private final AiDocumentRepository aiDocumentRepository;


    @Bean
    public Job reviewsDenormJob() {
        return new JobBuilder("reviewsDenormJob", jobRepository)
                .start(reviewsDenormStep())

                .build();
    }


    @Bean
    public Step reviewsDenormStep() {
        return new StepBuilder("reviewsDenormStep", jobRepository)
                .<List<AiFlatRow>, List<AiDocumentEntity>>chunk(1, transactionManager)
                .reader(flatRowsPageReader())
                .processor(this::toDocuments)
                .writer(docsChunk -> {                 // docsChunk = List<List<AiDocumentEntity>>
                    if (docsChunk.isEmpty()) return;
                    aiDocumentRepository.saveAll(docsChunk.getItems().get(0));
                })
                .build();
    }

    @Bean
    public ItemStreamReader<List<AiFlatRow>> flatRowsPageReader() {
        return new FlatRowsPageReader(storeRepository, 50);
    }


    private List<AiDocumentEntity> toDocuments(List<AiFlatRow> rows) {
        List<AiDocumentEntity> result = new ArrayList<>();
        if (rows == null || rows.isEmpty()) return result;

        // storeId -> 완성 중인 문서
        Map<UUID, AiDocumentEntity> storeMap = new LinkedHashMap<>();
        // storeId -> (menuId -> 메뉴)
        Map<UUID, Map<UUID, AiDocumentEntity.Menus>> menuMapByStore = new LinkedHashMap<>();

        for (AiFlatRow r : rows) {
            UUID storeId  = r.getStoreId();
            UUID menuId   = r.getMenuId();
            UUID reviewId = r.getReviewId();

            // 1) 스토어 문서 확보/생성
            AiDocumentEntity storeDoc = storeMap.get(storeId);
            if (storeDoc == null) {
                storeDoc = AiDocumentEntity.builder()
                        .id(storeId.toString())
                        .storeName(r.getStoreName())
                        .menus(new ArrayList<>())
                        .updateAt(LocalDateTime.now())
                        .build();
                storeMap.put(storeId, storeDoc);
                menuMapByStore.put(storeId, new LinkedHashMap<>());
            }

            // 2) 메뉴 확보/생성 + 스토어에 연결
            Map<UUID, AiDocumentEntity.Menus> menusOfStore = menuMapByStore.get(storeId);
            AiDocumentEntity.Menus menuDoc = menusOfStore.get(menuId);
            if (menuDoc == null) {
                menuDoc = AiDocumentEntity.Menus.builder()
                        .menuId(menuId.toString())
                        .menuName(r.getMenuName())
                        .reviews(new ArrayList<>())
                        .build();
                menusOfStore.put(menuId, menuDoc);
                storeDoc.getMenus().add(menuDoc);
            }

            // 3) 리뷰 추가
            AiDocumentEntity.Reviews reviewDoc = AiDocumentEntity.Reviews.builder()
                    .reviewId(reviewId.toString())
                    .text(r.getComment())
                    .createdAt(r.getCreatedAt())
                    .build();

            menuDoc.getReviews().add(reviewDoc);
        }

        return new ArrayList<>(storeMap.values());
    }


}
