package com.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final userService;
    private final userAddressService;

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signup(@RequestBody SignupRequestDto request) {
        String result = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<AuthResponseDTO.AuthRegisterResponseDTO>> registerCustomer(
            @Valid @RequestBody AuthRequestDTO.RegisterRequestDto request) {

        AuthResponseDTO.AuthRegisterResponseDTO response = authService.registerCustomer(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @PostMapping("/register/owner")
    public ResponseEntity<ApiResponse<AuthResponseDTO.AuthRegisterResponseDTO>> registerOwner(
            @Valid @RequestBody AuthRequestDTO.RegisterRequestDto request) {

        AuthResponseDTO.AuthRegisterResponseDTO response = authService.registerOwner(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withdraw(Authentication authentication) {
        String username = authentication.getName();
        String result = userService.softDeleteUser(username);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 회원 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoDto>> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserInfoDto userInfo = userService.getCurrentUser(username);
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    // 회원 정보 수정
    @PatchMapping
    public ResponseEntity<ApiResponse<?>> patchUser(
            @Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        String currentUserId = "U000000001";;//인증인가에서 가져온 유저 id
        String updateby="user";//인증인가에서 가져온 업데이트한 사람
        userService.updateUser(userUpdateRequestDto,currentUserId,updateby);
        return  new ResponseEntity<>(ApiResponse.success(null), HttpStatus.OK);
    }

    // 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AuthRequestDTO.PasswordChangeDto request) {

        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
    }
}
