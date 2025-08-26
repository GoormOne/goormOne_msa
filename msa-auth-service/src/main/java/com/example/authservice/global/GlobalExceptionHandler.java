package com.example.authservice.global;

import com.example.authservice.exception.BusinessException;
import com.example.common.dto.ApiResponse;
import com.example.common.dto.ErrorResponse;
import com.example.common.exception.CommonCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.rmi.ServerException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ApiResponse<?>> handleService(BusinessException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.failure(ErrorResponse.of(ex.getCode(), ex.getMessage(), req.getRequestURI())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpected(Exception ex, HttpServletRequest req) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.failure(ErrorResponse.of(5555, ex.getMessage(), req.getRequestURI())));
    }


    // 비즈니스 공통
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        return ApiResponse.fail(CommonCode.BAD_REQUEST, "error");
    }
}
