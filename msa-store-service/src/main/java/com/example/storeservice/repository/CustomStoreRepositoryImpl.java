package com.example.storeservice.repository;


import com.example.storeservice.dto.AiFlatRow;
import com.example.storeservice.entity.QMenu;
import com.example.storeservice.entity.QReview;
import com.example.storeservice.entity.QStore;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CustomStoreRepositoryImpl implements CustomStoreRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<AiFlatRow> findFlatRows(Collection<UUID> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return List.of();

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

    @Override
    public List<AiFlatRow> findFlatRowsPage(int page, int size) {
        // 1단계: 활성 스토어 ID 페이징
        QStore s = QStore.store;
        List<UUID> storeIds = queryFactory
                .select(s.storeId)
                .from(s)
                .where(s.isDeleted.isFalse())
                .orderBy(s.storeId.asc())
                .offset((long) page * size)
                .limit(size)
                .fetch();

        // 2단계: 해당 ID들에 대한 평탄화 데이터 조회
        return findFlatRows(storeIds);
    }

}