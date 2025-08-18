package com.example.storeservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_regions")
@Getter
@Setter
@NoArgsConstructor
public class Region {

    @Id
    @Column(name = "region_id", nullable = false)
    private UUID regionId;

    @Column(name = "region_1depth_name", length = 50)
    private String region1DepthName;

    @Column(name = "region_2depth_name", length = 50)
    private String region2DepthName;

    @Column(name = "region_3depth_name", length = 50)
    private String region3DepthName;


    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    private List<StoreRegion> storeRegions = new ArrayList<>();
}