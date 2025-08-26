package com.example.authservice.entity.admin;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "p_admin")
public class Admin {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "admin_id", columnDefinition = "uuid")
    private UUID adminId;

    @Column(length = 10, nullable = false, unique = true)
    private String username;

    @Column(length = 60, nullable = false)
    private String password;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(length = 30, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(nullable = false)
    private boolean isBanned;
}