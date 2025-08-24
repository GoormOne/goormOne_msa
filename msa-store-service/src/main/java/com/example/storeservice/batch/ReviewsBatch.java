package com.example.storeservice.batch;

import com.example.storeservice.dto.AiFlatRow;
import com.example.storeservice.mongoDB.AiDocumentEntity;
import com.example.storeservice.mongoDB.AiDocumentRepository;
import com.example.storeservice.mongoDB.StoreBatchQueryRepository;
import com.example.storeservice.repository.StoreRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

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
    private final EntityManagerFactory emf;

    private final StoreBatchQueryRepository storeBatchQueryRepository;
    private final AiDocumentRepository aiDocumentRepository;


    @Bean
    public Job reviewsDenormJob() {
        return new JobBuilder("reviewsDenormJob", jobRepository)
                .start(reviewsDenormStep(null, null, null)) // proxy 생성용, 실제 값은 StepScope에서 주입
                .build();
    }

    /**
     * Step: Reader(UUID: storeId) -> Processor(UUID -> AiDocumentEntity) -> Writer(Mongo save)
     */
    @Bean
    public Step reviewsDenormStep(
            ItemReader<UUID> storeIdReader,
            ItemProcessor<UUID, AiDocumentEntity> storeToDocProcessor,
            RepositoryItemWriter<AiDocumentEntity> aiDocumentWriter
    ) {
        return new StepBuilder("reviewsDenormStep", jobRepository)
                // 스토어 50개 단위로 커밋 (JobParam 또는 기본값)
                .<UUID, AiDocumentEntity>chunk(50, transactionManager)
                .reader(storeIdReader)
                .processor(storeToDocProcessor)
                .writer(aiDocumentWriter)
                .build();
    }

    /**
     * Reader: isDeleted=false 인 Store의 storeId만 JPQL로 페이지네이션해서 읽기
     * - pageSize: 한 번에 읽을 스토어 수(= 청크 후보)
     */
    @Bean
    @StepScope
    public JpaPagingItemReader<UUID> storeIdReader(
            @Value("#{jobParameters['pageSize']}") Integer pageSizeParam
    ) {
        int pageSize = (pageSizeParam == null || pageSizeParam <= 0) ? 50 : pageSizeParam;

        // select s.storeId ... JPQL (엔티티명/필드명은 네 프로젝트에 맞게)
        return new JpaPagingItemReaderBuilder<UUID>()
                .name("storeIdReader")
                .entityManagerFactory(emf)
                .queryString("""
                        select s.storeId
                        from Store s
                        where s.isDeleted = false
                        order by s.storeId
                        """)
                .pageSize(pageSize)
                .build();
    }

    /**
     * Processor: storeId 하나를 받아 → 해당 storeId의 모든 리뷰를 플랫으로 조회 → 문서로 집계
     * - 조회가 없거나 비어있으면 null 반환해서 writer로 가지 않게 함.
     * - @Transactional(readOnly=true)로 묶어 주면 JPA 2차 캐시/연결 이점 약간 있음.
     */
    @Bean
    @StepScope
    @Transactional(readOnly = true)
    public ItemProcessor<UUID, AiDocumentEntity> storeToDocProcessor() {
        return storeId -> {
            // 기존 네 JPQL 메서드 재사용: 단일 storeId로 조회
            List<AiFlatRow> rows = storeBatchQueryRepository.findFlatRows(List.of(storeId));
            if (rows == null || rows.isEmpty()) {
                return null; // 스킵
            }
            // 플랫 로우들을 스토어 문서로 집계
            return AiDocumentEntity.fromFlatRows(storeId, rows);
        };
    }

    /**
     * Writer: MongoRepository.save(...) → upsert 동작 (id가 같으면 덮어쓰기)
     * - AiDocumentEntity의 id를 storeId로 맞춰 두면 멱등성 확보.
     */
    @Bean
    public RepositoryItemWriter<AiDocumentEntity> aiDocumentWriter() {
        return new RepositoryItemWriterBuilder<AiDocumentEntity>()
                .repository(aiDocumentRepository)
                .methodName("save")
                .build();
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
                        .storeId(storeId)
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
                        .menuId(menuId)
                        .menuName(r.getMenuName())
                        .reviews(new ArrayList<>())
                        .build();
                menusOfStore.put(menuId, menuDoc);
                storeDoc.getMenus().add(menuDoc);
            }

            // 3) 리뷰 추가
            AiDocumentEntity.Reviews reviewDoc = AiDocumentEntity.Reviews.builder()
                    .reviewId(reviewId)
                    .text(r.getComment())
                    .createAt(r.getCreatedAt())
                    .build();

            menuDoc.getReviews().add(reviewDoc);
        }

        // 최종 문서 리스트 반환
        return new ArrayList<>(storeMap.values());
    }


}
