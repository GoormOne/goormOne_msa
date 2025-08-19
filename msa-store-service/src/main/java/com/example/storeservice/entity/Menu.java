package com.example.storeservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_menus")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Menu {

    @Id
    @Column(name = "menu_id", nullable = false)
    private UUID menuId; // PK = p_audit.audit_id

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_category_id", nullable = false)
    private MenuCategory menuCategory;

    @Column(name = "menu_name", length = 20, nullable = false)
    private String menuName;

    @Column(name = "menu_price", nullable = false)
    private Integer menuPrice;

    @Column(name = "menu_description", columnDefinition = "TEXT", nullable = false)
    private String menuDescription;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "menu_photo_url", length = 100)
    private String menuPhotoUrl;

    // nullable 허용 (DDL에서 NOT NULL 아님)
    @Column(name = "is_public_photo")
    private Boolean isPublicPhoto;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}