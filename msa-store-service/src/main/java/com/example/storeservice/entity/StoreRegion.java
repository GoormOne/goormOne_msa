package com.example.storeservice.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;



import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "p_stores_regions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "region_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreRegion {

    @Id
    @Column(name = "store_region_id", nullable = false)
    private UUID storeRegionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
}
