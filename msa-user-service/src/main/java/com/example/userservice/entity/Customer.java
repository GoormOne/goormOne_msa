package com.example.userservice.entity;

import com.example.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "p_customers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_p_customers_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_p_customers_email", columnNames = "email")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Customer {

    /* DB가 gen_random_uuid()로 생성하더라도, JPA가 자신이 ID를 넣어야 하나 오해하면 INSERT 안됨.
    -> JPA에도 UUID 자동 생성 전략을 명시 */
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "customer_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID customerId;

    @PrePersist
    void pre() { if (customerId == null) customerId = UUID.randomUUID(); }

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

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;
}
