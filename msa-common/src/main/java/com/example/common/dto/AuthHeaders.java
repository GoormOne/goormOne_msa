package com.example.common.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuthHeaders {
    private String groups;     // CUSTOMER | OWNER | ADMIN
    private String userId;     // UUID (문자열)
    private String userName;
    private String email;
    private String userRoles;  // 콤마로 조인된 전체 그룹(옵션)
}
