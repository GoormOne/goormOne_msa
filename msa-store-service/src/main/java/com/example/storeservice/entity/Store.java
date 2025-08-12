package com.example.storeservice.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_stores")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Store {

    @Id
    @Column(name = "store_id", nullable = false)
    private UUID storeId; // PK = p_audit.audit_id (DB에서 생성된 UUID 사용)

    // FK: p_owner.owner_id
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    // FK: p_stores_category.stores_category_id
    @Column(name = "stores_category_id", nullable = false)
    private UUID storesCategoryId;

    @Column(name = "store_name", length = 30, nullable = false)
    private String storeName;

    @Column(name = "store_description", columnDefinition = "TEXT", nullable = false)
    private String storeDescription;

    @Column(name = "address1", length = 50, nullable = false)
    private String address1;

    @Column(name = "address2", length = 50, nullable = false)
    private String address2;

    @Column(name = "zip_cd", length = 6, nullable = false)
    private String zipCd;

    @Column(name = "store_phone", length = 15, nullable = false)
    private String storePhone;

    @Column(name = "store_latitude", precision = 10, scale = 6, nullable = false)
    private BigDecimal storeLatitude;

    @Column(name = "store_longitude", precision = 10, scale = 6, nullable = false)
    private BigDecimal storeLongitude;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Builder.Default
    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;

    // 양방향 매핑 (선택)
    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreRegion> storeRegions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuCategory> menuCategories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();
}