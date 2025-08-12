package com.example.userservice.repository;

import com.profect.delivery.global.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress , UUID> {
    List<UserAddress> findByUserId(String userId);

}
