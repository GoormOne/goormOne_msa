package com.example.storeservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "p_menu_category",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "menu_category_name"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MenuCategory {

    @Id
    @Column(name = "menu_category_id", nullable = false)
    private UUID menuCategoryId; // PK = p_audit.audit_id

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "menu_category_name", length = 50, nullable = false)
    private String menuCategoryName;

    @Builder.Default
    @OneToMany(mappedBy = "menuCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    public MenuCategory(UUID menuCategoryId) {
        this.menuCategoryId = menuCategoryId;
    }

    public MenuCategory(UUID pk, UUID storeId, String menuCategoryName) {
        this.menuCategoryId = pk;
        this.store =  new Store(storeId);
        this.menuCategoryName = menuCategoryName;
    }
}