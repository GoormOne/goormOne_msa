package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "p_users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID id; // = p_audit.audit_id (공유 PK)

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @MapsId // user_id == audit.audit_id
    @JoinColumn(name = "user_id", referencedColumnName = "audit_id")
    private Audit audit;

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

    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;
}
