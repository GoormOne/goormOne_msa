//package com.example.userservice.service;
//
//import com.example.userservice.repository.UserRepository;
//import com.example.userservice.dto.AuthRequestDTO;
//import com.example.userservice.dto.AuthResponseDTO;
//import com.example.userservice.dto.LoginResponseDTO;
//import com.example.userservice.jwt.JwtTokenProvider;
//import com.example.userservice.entity.Role;
//import com.example.userservice.entity.User;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.sql.Timestamp;
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtTokenProvider jwtTokenProvider;
//
//    public AuthResponseDTO.AuthRegisterResponseDTO registerCustomer(AuthRequestDTO.RegisterRequestDto request) {
//        if (userRepository.existsById(request.getUserId())) {
//            throw new IllegalArgumentException("이미 존재하는 사용자 ID입니다.");
//        }
//
//        User user = User.builder()
//                .userId(request.getUserId())
//                .username(request.getUsername())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .name(request.getName())
//                .birth(request.getBirth())
//                .email(request.getEmail())
//                .role(Role.CUSTOMER)
//                .isPublic(true)
//                .isBanned(false)
//                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
//                .createdBy(request.getUserId())
//                .build();
//
//        userRepository.save(user);
//
//        return new AuthResponseDTO.AuthRegisterResponseDTO(
//                user.getUserId(),
//                user.getUsername(),
//                "고객 회원가입이 완료되었습니다."
//        );
//    }
//
//    public AuthResponseDTO.AuthRegisterResponseDTO registerOwner(AuthRequestDTO.RegisterRequestDto request) {
//        if (userRepository.existsById(request.getUserId())) {
//            throw new IllegalArgumentException("이미 존재하는 사용자 ID입니다.");
//        }
//
//        User user = User.builder()
//                .userId(request.getUserId())
//                .username(request.getUsername())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .name(request.getName())
//                .birth(request.getBirth())
//                .email(request.getEmail())
//                .role(Role.OWNER)
//                .isPublic(true)
//                .isBanned(false)
//                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
//                .createdBy(request.getUserId())
//                .build();
//
//        userRepository.save(user);
//
//        return new AuthResponseDTO.AuthRegisterResponseDTO(
//                user.getUserId(),
//                user.getUsername(),
//                "사장 회원가입이 완료되었습니다."
//        );
//    }
//
//    public LoginResponseDTO login(AuthRequestDTO.LoginRequestDto request) {
//        User user = userRepository.findById(request.getUserId())
//                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
//
//        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
//        }
//
//        if (user.isBanned()) {
//            throw new IllegalStateException("차단된 사용자입니다.");
//        }
//
//        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getRole());
//        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
//
//        return new LoginResponseDTO(accessToken, refreshToken, user.getUserId(), user.getRole().name());
//    }
//
//    public void changePassword(String userId, AuthRequestDTO.PasswordChangeDto request) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
//
//        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
//            throw new BadCredentialsException("현재 비밀번호가 일치하지 않습니다.");
//        }
//
//        user.update(null, passwordEncoder.encode(request.getNewPassword()), null, null, userId);
//        userRepository.save(user);
//    }
//
//    public LoginResponseDTO refreshToken(String refreshToken) {
//        if (!jwtTokenProvider.isValidToken(refreshToken)) {
//            throw new IllegalArgumentException("유효하지 않은 RefreshToken입니다.");
//        }
//
//        String userId = jwtTokenProvider.getIdFromToken(refreshToken);
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
//
//        String newAccessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getRole());
//        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
//
//        return new LoginResponseDTO(newAccessToken, newRefreshToken, user.getUserId(), user.getRole().name());
//    }
//}