package com.example.storeservice.global;


import com.example.common.dto.ApiResponse;
import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;
import com.example.storeservice.global.exception.StoreAlreadyDeletedException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StoreAlreadyDeletedException.class)
    public ResponseEntity<ApiResponse<?>> handleStoreAlreadyDeleted(StoreAlreadyDeletedException ex) {
        log.warn("Store already deleted: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(CommonCode.STORE_DELETED));
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Invalid parameter: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(CommonCode.INVALID_UUID));
    }

    // 404 - 엔티티 없음
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(CommonCode.NOT_FOUND));
    }

    // 403 - 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(CommonCode.FORBIDDEN));
    }

    // 400 - 바인딩/검증 실패 (DTO @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("validation error");
        log.warn("Validation failed: {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(CommonCode.BAD_REQUEST, msg));
    }

    // 400 - 제약 위반 (컨트롤러 파라미터 @Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(CommonCode.BAD_REQUEST));
    }

    // 컨트롤러에서 명시적으로 상태를 던진 경우
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        log.warn("ResponseStatusException: status={}, reason={}", ex.getStatusCode(), ex.getReason());
        return ResponseEntity.status(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(mapStatusToCommonCode(status)));
    }

    // 500 - 그 외 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleOthers(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(CommonCode.INTERNAL_SERVER_ERROR));
    }

    // 비즈니스 예외 공통 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException ex) {
        log.warn("BusinessException: code={}, detail={}", ex.getCode(), ex.getDetail());
        var code = ex.getCode();
        String msg = ex.getDetail() != null ? ex.getDetail() : code.getMessage();
        return ResponseEntity.status(code.getHttpStatus())
            .body(ApiResponse.fail(code, msg));
    }

    // 낙관적 락 충돌 → 409
    @ExceptionHandler({jakarta.persistence.OptimisticLockException.class,
        org.hibernate.StaleObjectStateException.class})
    public ResponseEntity<ApiResponse<?>> handleOptimisticLock(Exception ex) {
        log.warn("Optimistic lock conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.fail(CommonCode.CONCURRENCY_CONFLICT));
    }

    private CommonCode mapStatusToCommonCode(HttpStatus status) {
        if (status == null) return CommonCode.INTERNAL_SERVER_ERROR;
        return switch (status) {
            case BAD_REQUEST -> CommonCode.BAD_REQUEST;
            case UNAUTHORIZED -> CommonCode.UNAUTHORIZED;
            case FORBIDDEN -> CommonCode.FORBIDDEN;
            case NOT_FOUND -> CommonCode.NOT_FOUND;
            default -> CommonCode.INTERNAL_SERVER_ERROR;
        };
    }
}
