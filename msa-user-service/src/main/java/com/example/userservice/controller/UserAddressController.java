package com.example.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public class UserAddressController {
//    // 주소지 목록 조회
//    @GetMapping("/addresses")
//    public ResponseEntity<ApiResponse<List<UserAddressesResponseDto>>> getUserAddresses() {
//        String currentUserId = "U000000011";
//        List<UserAddressesResponseDto> addressesDto = userAddressService.findByUserId(currentUserId);
//        return new ResponseEntity<>(ApiResponse.success(addressesDto), HttpStatus.OK);
//    }
//
//    // 주소지 등록
//    @PostMapping("/addresses")
//    public ResponseEntity<ApiResponse<?>> postUserAddressses(
//            @RequestBody UserAddressesRequestDto userAddressesRequestDto) {
//        String currentUserId = "U000000011";
//        userAddressService.CreateUserAddress(userAddressesRequestDto,currentUserId);
//        return  new ResponseEntity<>(ApiResponse.success(null), HttpStatus.CREATED);
//    }
//
//    // 주소지 수정
//
//    // 주소지 삭제
//
//    // 기본 주소지 설정

//    •	GET /users/me/addresses?size=10
//    •	POST /users/me/addresses
//	  •	PUT /users/me/addresses/{id}
//	  •	DELETE /users/me/addresses/{id}
//	  •	PATCH /users/me/addresses/{id}/default

}
