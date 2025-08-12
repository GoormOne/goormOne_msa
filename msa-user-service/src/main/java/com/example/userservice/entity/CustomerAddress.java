package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_customer_address")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CustomerAddress {

    @Id
    @Column(name = "address_id", nullable = false)
    private UUID id; // = p_audit.audit_id (공유 PK)

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @MapsId // address_id == audit.audit_id
    @JoinColumn(name = "address_id", referencedColumnName = "audit_id")
    private Audit audit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "address_name", nullable = false, length = 20)
    private String addressName;

    @Column(name = "address1", nullable = false, length = 50)
    private String address1;

    @Column(name = "address2", nullable = false, length = 50)
    private String address2;

    @Column(name = "zip_cd", nullable = false, length = 6)
    private String zipCode;

    @Column(name = "user_latitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "user_longitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
