package com.example.storeservice.repository;

import com.example.storeservice.dto.AiFlatRow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// 배치 전용 조회
@Repository
@RequiredArgsConstructor
public class StoreBatchQueryRepository {

    @PersistenceContext
    private EntityManager em;

    public List<AiFlatRow> findFlatRows(Collection<UUID> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return List.of();

        return em.createQuery("""
            select new com.example.storeservice.dto.AiFlatRow(
                s.storeId, s.storeName,
                m.menuId, m.menuName,
                r.reviewId, r.comment, r.createdAt
            )
            from Store s
            join s.menus m
            join m.reviews r
            where s.storeId in :storeIds
            order by s.storeId, m.menuId, r.reviewId
            """, AiFlatRow.class)
                .setParameter("storeIds", storeIds)
                .getResultList();

    }
}
