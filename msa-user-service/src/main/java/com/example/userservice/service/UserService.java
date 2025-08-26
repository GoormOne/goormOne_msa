package com.example.userservice.service;

import com.example.userservice.dto.ChangePasswordReq;
import com.example.userservice.dto.MyProfileRes;
import com.example.userservice.dto.UpdateCustomerReq;
import com.example.userservice.dto.UpdateOwnerReq;
import com.example.userservice.entity.Customer;
import com.example.userservice.entity.CustomerAudit;
import com.example.userservice.entity.Owner;
import com.example.userservice.entity.OwnerAudit;
import com.example.userservice.repository.CustomerAuditRepository;
import com.example.userservice.repository.CustomerRepository;
import com.example.userservice.repository.OwnerAuditRepository;
import com.example.userservice.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CustomerRepository customerRepository;
    private final OwnerRepository ownerRepository;
    private final CustomerAuditRepository customerAuditRepository;
    private final OwnerAuditRepository ownerAuditRepository;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // === CUSTOMER ===
    @Transactional
    public MyProfileRes getMyCustomer(UUID customerId) {
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("CUSTOMER_NOT_FOUND"));

        return MyProfileRes.builder()
                .id(c.getCustomerId().toString())
                .username(c.getUsername())
                .name(c.getName())
                .email(c.getEmail())
                .birth(c.getBirth().toString())
                .emailVerified(Boolean.TRUE.equals(c.getEmailVerified()))
                .banned(Boolean.TRUE.equals(c.getIsBanned()))
                .build();
    }

    @Transactional
    public MyProfileRes updateMyCustomer(UUID customerId, UpdateCustomerReq req) {
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("CUSTOMER_NOT_FOUND"));

        if (req.getName() != null) c.setName(req.getName());
        if (req.getEmail() != null) c.setEmail(req.getEmail());
        if (req.getBirth() != null) c.setBirth(LocalDate.parse(req.getBirth()));

        customerRepository.save(c);

        CustomerAudit audit = customerAuditRepository.findById(customerId)
                .orElseGet(() -> {
                    CustomerAudit a = new CustomerAudit();
                    a.setAuditId(customerId);
                    a.setCreatedAt(OffsetDateTime.now());
                    a.setCreatedBy(customerId);
                    return a;
                });
        audit.setUpdatedAt(OffsetDateTime.now());
        audit.setUpdatedBy(customerId);
        customerAuditRepository.save(audit);

        return getMyCustomer(customerId);
    }

    @Transactional
    public void changeCustomerPassword(UUID customerId, ChangePasswordReq req) {
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("CUSTOMER_NOT_FOUND"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), c.getPassword())) {
            throw new IllegalArgumentException("INVALID_CURRENT_PASSWORD");
        }
        c.setPassword(passwordEncoder.encode(req.getNewPassword()));
        customerRepository.save(c);

        CustomerAudit audit = customerAuditRepository.findById(customerId)
                .orElseGet(() -> {
                    CustomerAudit a = new CustomerAudit();
                    a.setAuditId(customerId);
                    a.setCreatedAt(OffsetDateTime.now());
                    a.setCreatedBy(customerId);
                    return a;
                });
        audit.setUpdatedAt(OffsetDateTime.now());
        audit.setUpdatedBy(customerId);
        customerAuditRepository.save(audit);
    }


    // ===== OWNER =====
    @Transactional
    public MyProfileRes getMyOwner(UUID ownerId) {
        Owner o = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("OWNER_NOT_FOUND"));

        return MyProfileRes.builder()
                .id(o.getOwnerId().toString())
                .username(o.getUsername())
                .name(o.getName())
                .email(o.getEmail())
                .birth(o.getBirth().toString())
                .emailVerified(Boolean.TRUE.equals(o.getEmailVerified()))
                .banned(Boolean.TRUE.equals(o.getIsBanned()))
                .build();
    }

    @Transactional
    public MyProfileRes updateMyOwner(UUID ownerId, UpdateOwnerReq req) {
        Owner o = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("OWNER_NOT_FOUND"));

        if (req.getName() != null) o.setName(req.getName());
        if (req.getEmail() != null) o.setEmail(req.getEmail());
        if (req.getBirth() != null) o.setBirth(LocalDate.parse(req.getBirth()));

        ownerRepository.save(o);

        OwnerAudit audit = ownerAuditRepository.findById(ownerId)
                .orElseGet(() -> {
                    OwnerAudit a = new OwnerAudit();
                    a.setAuditId(ownerId);
                    a.setCreatedAt(OffsetDateTime.now());
                    a.setCreatedBy(ownerId);
                    return a;
                });
        audit.setUpdatedAt(OffsetDateTime.now());
        audit.setUpdatedBy(ownerId);
        ownerAuditRepository.save(audit);

        return getMyOwner(ownerId);
    }

    @Transactional
    public void changeOwnerPassword(UUID ownerId, ChangePasswordReq req) {
        Owner o = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("OWNER_NOT_FOUND"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), o.getPassword())) {
            throw new IllegalArgumentException("INVALID_CURRENT_PASSWORD");
        }
        o.setPassword(passwordEncoder.encode(req.getNewPassword()));
        ownerRepository.save(o);

        OwnerAudit audit = ownerAuditRepository.findById(ownerId)
                .orElseGet(() -> {
                    OwnerAudit a = new OwnerAudit();
                    a.setAuditId(ownerId);
                    a.setCreatedAt(OffsetDateTime.now());
                    a.setCreatedBy(ownerId);
                    return a;
                });
        audit.setUpdatedAt(OffsetDateTime.now());
        audit.setUpdatedBy(ownerId);
        ownerAuditRepository.save(audit);
    }
}
