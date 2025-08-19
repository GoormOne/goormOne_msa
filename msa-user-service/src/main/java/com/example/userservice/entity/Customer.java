package com.example.userservice.entity;

import com.example.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "p_customers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class Customer extends AuditBaseEntity {

    /* DB가 gen_random_uuid()로 생성하더라도, JPA가 자신이 ID를 넣어야 하나 오해하면 INSERT 안됨.
    -> JPA에도 UUID 자동 생성 전략을 명시 */
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "customer_id", updatable = false, nullable = false)
    private UUID customerId;

//    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
//    @MapsId // user_id == p_user_audit.audit_id
//    @JoinColumn(name = "user_id", referencedColumnName = "audit_id")
//    private UserAudit userAudit;

    @Column(name = "username", nullable = false, unique = true, length = 10)
    private String username;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "email", nullable = false, unique = true, length = 30)
    private String email;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Builder.Default
    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;
}
