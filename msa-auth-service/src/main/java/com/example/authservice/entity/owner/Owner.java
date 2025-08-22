package com.example.authservice.entity.owner;

import com.example.authservice.entity.customer.CustomerAudit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "p_owners")
public class Owner {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "owner_id", columnDefinition = "uuid")
    private UUID ownerId;

    @Column(name = "username", length = 10, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Column(name = "name", length = 10, nullable = false)
    private String name;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "email", length = 30, nullable = false, unique = true)
    private String email;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "is_banned", nullable = false)
    @Builder.Default
    private boolean isBanned = false;

    @OneToOne(mappedBy = "owner",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            optional = false)
    private OwnerAudit audit;

    public void attachAudit(OwnerAudit audit) {
        this.audit = audit;
        audit.setOwner(this);
    }
}