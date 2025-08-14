package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "p_owners")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Owner {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "owner_id", updatable = false, nullable = false)
    private UUID ownerId;

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
