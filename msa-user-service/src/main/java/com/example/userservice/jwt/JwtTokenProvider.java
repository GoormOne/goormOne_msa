//package com.example.userservice.jwt;
//
//import com.example.userservice.config.JwtProperties;
//import com.example.userservice.dto.TokenDTO;
//import com.example.userservice.entity.Role;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.time.Instant;
//import java.util.Date;
//
//@Component
//@RequiredArgsConstructor
//public class JwtTokenProvider {
//
//    private final JwtProperties jwtProperties;
//
//    public TokenDTO createToken(String userId, Role role) {
//        String accessToken = Jwts.builder()
//                .subject(userId)
//                .claim("role", role.name())
//                .issuedAt(new Date())
//                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
//                .signWith(getSigningKey())
//                .compact();
//
//        String refreshToken = Jwts.builder()
//                .subject(userId)
//                .issuedAt(new Date())
//                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpiration()))
//                .signWith(getSigningKey())
//                .compact();
//        return new TokenDTO("Bearer", accessToken, refreshToken, jwtProperties.getExpiration());
//    }
//
//    public String getIdFromToken(String token, boolean isRefresh) {
//        return getClaims(token, isRefresh).getSubject();
//    }
//
//    public Role getRoleFromToken(String token) {
//        String role = (String) getClaims(token, false).get("role");
//        return Role.valueOf(role);
//    }
//
//    public boolean isValidToken(String token, boolean isRefresh) {
//        try {
//            getClaims(token, isRefresh);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    private Claims getClaims(String token, boolean isRefresh) {
//        String key = isRefresh ? jwtProperties.getRefreshSecret() : jwtProperties.getSecret();
//        return Jwts.parser()
//                .verifyWith(getSigningKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }
//
//    private SecretKey getSigningKey() {
//        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
//    }
//
//    public Instant getExpirationInstant(String token, boolean isRefresh) {
//        Claims claims = getClaims(token, isRefresh);
//        return claims.getExpiration().toInstant();
//    }
//}