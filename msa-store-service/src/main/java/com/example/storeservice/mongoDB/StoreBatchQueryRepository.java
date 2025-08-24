package com.example.storeservice.mongoDB;

import com.example.storeservice.dto.AiFlatRow;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// 배치 전용 조회
@Repository
@RequiredArgsConstructor
public class StoreBatchQueryRepository {
    private final JPAQueryFactory queryFactory;

    private EntityManager em;

//    public List<AiFlatRow> findFlatRows(Collection<UUID> storeIds) {
//        if (storeIds == null || storeIds.isEmpty()) return List.of();
//
//        return em.createQuery("""
//            select new com.example.storeservice.dto.AiFlatRow(
//                s.storeId, s.storeName,
//                m.menuId, m.menuName,
//                r.reviewId, r.comment, r.createdAt
//            )
//            from Store s
//            join s.menus m
//            join m.reviews r
//            where s.storeId in :storeIds
//            order by s.storeId, m.menuId, r.reviewId
//            """, AiFlatRow.class)
//                .setParameter("storeIds", storeIds)
//                .getResultList();
//
//    }

    public List<AiFlatRow> findFlatRowsPage(int page, int size) {
        QStore s = QStore.store;

        // 1단계: 페이지 대상 스토어 ID
        List<UUID> storeIds = queryFactory
                .select(s.storeId)
                .from(s)
                .where(s.isDeleted.isFalse())
                .orderBy(s.storeId.asc())
                .offset((long) page * size)
                .limit(size)
                .fetch();

        if (storeIds.isEmpty()) return List.of();

        // 2단계: 해당 스토어들의 메뉴/리뷰를 평탄화 DTO로 조회
        return fetchFlatRows(storeIds);
    }

    private List<AiFlatRow> fetchFlatRows(Collection<UUID> storeIds) {
        QStore s = QStore.store;
        QMenu m  = QMenu.menu;
        QReview r = QReview.review;

        return queryFactory
                .select(Projections.constructor(AiFlatRow.class,
                        s.storeId, s.storeName,
                        m.menuId, m.menuName,
                        r.reviewId, r.comment, r.createdAt))
                .from(s)
                .join(s.menus, m)
                .join(m.reviews, r)
                .where(s.storeId.in(storeIds))
                .orderBy(s.storeId.asc(), m.menuId.asc(), r.reviewId.asc())
                .fetch();
    }

    // 전체 스토어 수(페이지 계산용)
    public long countActiveStores() {
        QStore s = QStore.store;
        return queryFactory
                .select(s.count())
                .from(s)
                .where(s.isDeleted.isFalse())
                .fetchOne();
    }
}
