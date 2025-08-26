package com.example.userservice.controller;

import com.example.userservice.service.UserResolveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserResolveService resolveService;

    // GET /internal/users/resolve?username=...&email=...&group=CUSTOMER|OWNER|ADMIN
    // GW의 UserResolverClient가 호출
//    @GetMapping("/resolve")
//    public ResponseEntity<?> resolve(@RequestParam String username,
//                                     @RequestParam String email,
//                                     @RequestParam String group) {
//        var res = userResolveService.resolve(username, email, group);
//        return ResponseEntity.ok(new ResolveRes(res.principalType(), res.userId(), res.username(), res.email(), new String[]{group}));
//    }
//
//    public record ResolveRes(String principalType, String userId, String username, String email, String[] roles) {}

    @GetMapping("/resolve")
    public Map<String,Object> resolve(@RequestParam String username,
                                      @RequestParam String email,
                                      @RequestParam String group) {
        var r = resolveService.resolve(username, email, group);
        return Map.of(
                "principalType", r.getPrincipalType(),
                "userId", r.getUserId().toString(),
                "username", r.getUsername(),
                "email", r.getEmail(),
                "roles", List.of(r.getPrincipalType())
        );
    }
}