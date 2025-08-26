package com.example.userservice.service;

import com.example.userservice.repository.CustomerRepository;
import com.example.userservice.repository.OwnerRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserResolveService {

    private final CustomerRepository customerRepo;
    private final OwnerRepository ownerRepo;

    public Resolved resolve(String username, String email, String group) {
        String g = group == null ? "" : group.trim().toUpperCase();
        UUID id = switch (g) {
            case "OWNER"   -> ownerRepo.findIdByUsername(username)
                    .or(() -> ownerRepo.findIdByEmail(email))
                    .orElseThrow(() -> new IllegalArgumentException("OWNER_NOT_FOUND"));
            case "ADMIN"   -> throw new IllegalArgumentException("ADMIN_RESOLVE_NOT_IMPLEMENTED");
            default -> customerRepo.findIdByUsername(username)
                    .or(() -> customerRepo.findIdByEmail(email))
                    .orElseThrow(() -> new IllegalArgumentException("CUSTOMER_NOT_FOUND"));
        };
        return new Resolved(g.isEmpty() ? "CUSTOMER" : g, id, username, email);
    }

    @Getter
    @AllArgsConstructor
    public static class Resolved {
        private final String principalType;
        private final UUID userId;
        private final String username;
        private final String email;
    }
}