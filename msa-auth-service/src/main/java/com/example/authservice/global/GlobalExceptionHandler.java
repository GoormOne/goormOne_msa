package com.example.authservice.global;

import com.example.authservice.exception.ServiceException;
import com.example.common.dto.ApiResponse;
import com.example.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.rmi.ServerException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ApiResponse<?>> handleService(ServiceException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.failure(ErrorResponse.of(ex.getCode(), ex.getMessage(), req.getRequestURI())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpected(Exception ex, HttpServletRequest req) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.failure(ErrorResponse.of(5555, ex.getMessage(), req.getRequestURI())));
    }
}
