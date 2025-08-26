package com.example.common.web;

import com.example.common.dto.AuthHeaders;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.method.support.*;
import org.springframework.web.context.request.NativeWebRequest;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String H_X_GROUPS     = "X-Groups";
    private static final String H_X_USER_ID    = "X-User-Id";
    private static final String H_X_USER_NAME  = "X-User-Name";
    private static final String H_X_USER_ROLES = "X-User-Roles";
    private static final String H_X_EMAIL      = "X-Email";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class)
                && parameter.getParameterType().isAssignableFrom(AuthHeaders.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest req = (HttpServletRequest) webRequest.getNativeRequest();
        AuthHeaders headers = AuthHeaders.builder()
                .groups(safe(req.getHeader(H_X_GROUPS)))
                .userId(safe(req.getHeader(H_X_USER_ID)))
                .userName(safe(req.getHeader(H_X_USER_NAME)))
                .userRoles(safe(req.getHeader(H_X_USER_ROLES)))
                .email(safe(req.getHeader(H_X_EMAIL)))
                .build();
        return headers;
    }

    private String safe(String v) { return v == null ? "" : v; }
}