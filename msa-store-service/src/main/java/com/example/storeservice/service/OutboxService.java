package com.example.storeservice.service;

import com.example.storeservice.entity.*;
import com.example.storeservice.global.EventAction;
import com.example.storeservice.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    public UUID insertOutbox(Object entity, long version, EventAction action) {
        ObjectNode payload;
        String aggregateType;
        String aggregateId;

        if (entity instanceof Store store) {
            aggregateType = "Store";
            aggregateId   = store.getStoreId().toString();
            payload       = (action == EventAction.DELETED)
                    ? minimalDeletePayload("storeId", aggregateId) // 삭제일 땐 최소 페이로드 권장
                    : getStoreObject(store);
        } else if (entity instanceof Menu menu) {
            aggregateType = "Menu";
            aggregateId   = menu.getMenuId().toString();
            payload       = (action == EventAction.DELETED)
                    ? minimalDeletePayload("menuId", aggregateId)
                    : getMenuObject(menu);
        } else if (entity instanceof MenuCategory category) {
            aggregateType = "MenuCategory";
            aggregateId   = category.getMenuCategoryId().toString();
            payload       = (action == EventAction.DELETED)
                    ? minimalDeletePayload("menuCategoryId", aggregateId)
                    : getMenuCategoryObject(category);
        } else if (entity instanceof Review review) {
            aggregateType = "Review";
            aggregateId   = review.getReviewId().toString();
            payload       = (action == EventAction.DELETED)
                    ? minimalDeletePayload("reviewId", aggregateId)
                    : getReviewObject(review);
        } else {
            throw new IllegalArgumentException("지원하지 않는 엔티티 타입: " + entity.getClass());
        }

        // "StoreCreated", "MenuUpdated", "ReviewDeleted" 같은 형태로 자동 조합
        String eventType = aggregateType + action.suffix();

        OutboxEntity outbox = OutboxEntity.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .version(version)
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepository.save(outbox);
        return outbox.getEventId();
    }


    /** 삭제 이벤트일 때 최소 페이로드(멱등 신호 포함) */
    private ObjectNode minimalDeletePayload(String idField, String idValue) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put(idField, idValue);
        node.put("__deleted", true);  // 다운스트림이 물리/논리 삭제 판단에 사용
        return node;
    }

    private ObjectNode getStoreObject(Store store){
        ObjectNode node = objectMapper.createObjectNode();

        node.put("storeId", store.getStoreId().toString());
        node.put("ownerId", store.getOwnerId().toString());
        node.put("storesCategoryId", store.getStoresCategoryId().toString());
        node.put("storeName", store.getStoreName());
        node.put("storeDescription", store.getStoreDescription());
        node.put("address1", store.getAddress1());
        node.put("address2", store.getAddress2());
        node.put("zipCd", store.getZipCd());
        node.put("storePhone", store.getStorePhone());

        if (store.getStoreLatitude() != null) {
            node.put("storeLatitude", store.getStoreLatitude());
        }
        if (store.getStoreLongitude() != null) {
            node.put("storeLongitude", store.getStoreLongitude());
        }

        if (store.getOpenTime() != null) {
            node.put("openTime", store.getOpenTime().toString());
        }
        if (store.getCloseTime() != null) {
            node.put("closeTime", store.getCloseTime().toString());
        }

        node.put("isBanned", store.getIsBanned() != null ? store.getIsBanned() : false);
        node.put("isDeleted", store.getIsDeleted() != null ? store.getIsDeleted() : false);


        return node;
    }

    // Menu → ObjectNode
    private ObjectNode getMenuObject(Menu menu) {
        ObjectNode node = objectMapper.createObjectNode();

        node.put("menuId", menu.getMenuId().toString());
        node.put("storeId", menu.getStore().getStoreId().toString());
        node.put("menuCategoryId", menu.getMenuCategory().getMenuCategoryId().toString());

        node.put("menuName", menu.getMenuName());
        node.put("menuPrice", menu.getMenuPrice());
        node.put("menuDescription", menu.getMenuDescription());
        node.put("isPublic", menu.getIsPublic() != null ? menu.getIsPublic() : false);
        node.put("menuPhotoUrl", menu.getMenuPhotoUrl());
        node.put("isPublicPhoto", menu.getIsPublicPhoto() != null ? menu.getIsPublicPhoto() : false);
        node.put("isDeleted", menu.getIsDeleted() != null ? menu.getIsDeleted() : false);

        return node;
    }

    // MenuCategory → ObjectNode
    private ObjectNode getMenuCategoryObject(MenuCategory category) {
        ObjectNode node = objectMapper.createObjectNode();

        node.put("menuCategoryId", category.getMenuCategoryId().toString());
        node.put("storeId", category.getStore().getStoreId().toString());
        node.put("menuCategoryName", category.getMenuCategoryName());
        node.put("isDeleted", category.getIsDeleted() != null ? category.getIsDeleted() : false);

        return node;
    }

    // Review → ObjectNode
    private ObjectNode getReviewObject(Review review) {
        ObjectNode node = objectMapper.createObjectNode();

        node.put("reviewId", review.getReviewId().toString());
        node.put("storeId", review.getStore().getStoreId().toString());
        node.put("menuId", review.getMenu().getMenuId().toString());
        node.put("customerId", review.getCustomerId().toString());
        node.put("rating", review.getRating());
        node.put("comment", review.getComment());

        node.put("isPublic", review.getIsPublic() != null ? review.getIsPublic() : true);
        node.put("isDeleted", review.getIsDeleted() != null ? review.getIsDeleted() : false);

        if (review.getCreatedAt() != null) {
            node.put("createdAt", review.getCreatedAt().toString());
        }

        return node;
    }


}
