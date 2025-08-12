package com.example.userservice.service;

import com.profect.delivery.domain.users.dto.request.UserAddressesRequestDto;
import com.profect.delivery.domain.users.dto.response.UserAddressesResponseDto;
import com.profect.delivery.domain.users.repository.UserAdressRepository;
import com.profect.delivery.global.entity.UserAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAddressService {

    private final UserAdressRepository userAddressRepository;



    @Transactional(readOnly = true)
    public List<UserAddressesResponseDto> findByUserId(String userId) {
        // 1. Repository를 통해 주소 엔티티 목록을 조회합니다.
        List<UserAddress> addresses = userAddressRepository.findByUserId(userId);

        return addresses.stream()
                .map(UserAddressesResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public void CreateUserAddress(UserAddressesRequestDto userAddressDto,String UserId) {
        UserAddress userAddress = userAddressDto.toEntity(UserId);

        userAddressRepository.save(userAddress);
    }
}