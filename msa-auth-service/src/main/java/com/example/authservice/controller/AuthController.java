package com.example.authservice.controller;

import com.example.authservice.dto.CustomerRegisterReq;
import com.example.authservice.dto.CustomerRegisterRes;
import com.example.authservice.dto.OwnerRegisterReq;
import com.example.authservice.dto.OwnerRegisterRes;
import com.example.authservice.service.AuthService;
import com.example.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
//    •	POST /auth/register (username/password)
//	  •	POST /auth/login → JWT
//	  •	POST /auth/password/change
//	  •	POST /auth/token/revoke
    private final AuthService authService;

    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<CustomerRegisterRes>> registerCustomer(@Valid @RequestBody CustomerRegisterReq req) {
        var res = authService.registerCustomer(req);
        return ResponseEntity.status(201).body(ApiResponse.success(res));
    }

    @PostMapping("/register/owner")
    public ResponseEntity<ApiResponse<OwnerRegisterRes>> registerOwner(@Valid @RequestBody OwnerRegisterReq req) {
        var res = authService.registerOwner(req);
        return ResponseEntity.status(201).body(ApiResponse.success(res));
    }
}
