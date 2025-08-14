package com.example.userservice.repository;

import com.example.userservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
}

// 기존 코드 주석처리
/*
import com.profect.delivery.global.entity.User;
public interface UserRepository extends JpaRepository<User, String> {
    User findByUserId(String userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
*/
