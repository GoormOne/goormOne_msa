package com.example.common.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.example.common.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order
@RestControllerAdvice(basePackages = "com.example")
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusiness (BusinessException ex, HttpServletRequest req) {
		log.error("[BUSINESS] code={} uri={} msg={} detail={}",
			ex.getCode().getCode(), req.getRequestURI(), ex.getMessage(), ex.getDetail(), ex);

		return ResponseEntity
			.status(ex.getCode().getHttpStatus())
			.body(ApiResponse.fail(ex.getCode(),
				ex.getDetail() != null ? ex.getDetail() : ex.getCode().getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation (MethodArgumentNotValidException ex, HttpServletRequest req) {
		String msg = ex.getBindingResult().getFieldErrors().stream()
			.map(fe -> fe.getField() + "=" + fe.getDefaultMessage())
			.findFirst().orElse(CommonCode.BAD_REQUEST.getMessage());

		log.warn("[VALID] uri ={} msg={}", req.getRequestURI(), msg);
		return ResponseEntity
			.status(CommonCode.BAD_REQUEST.getHttpStatus())
			.body(ApiResponse.fail(CommonCode.BAD_REQUEST, msg));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotReadable (HttpMessageNotReadableException ex, HttpServletRequest req) {
		log.warn("[NOT_READABLE] uri={} msg={}", req.getRequestURI(), ex.getMessage());
		return ResponseEntity
			.status(CommonCode.BAD_REQUEST.getHttpStatus())
			.body(ApiResponse.fail(CommonCode.BAD_REQUEST, "요청 본문을 읽을 수 없습니다."));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingParam (MissingServletRequestParameterException ex, HttpServletRequest req) {
		String msg = "필수 파라미터 누락: " + ex.getParameterName();
		log.warn("[MISSING_PARAM] uri={} msg={} ", req.getRequestURI(), msg);
		return ResponseEntity
			.status(CommonCode.BAD_REQUEST.getHttpStatus())
			.body(ApiResponse.fail(CommonCode.BAD_REQUEST, msg));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported (HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
		log.warn("[METHOD_NOT_SUPPORTED] uri={} method={}", req.getRequestURI(), ex.getMethod() );
		return ResponseEntity
			.status(CommonCode.BAD_REQUEST.getHttpStatus())
			.body(ApiResponse.fail(CommonCode.BAD_REQUEST, "지원하지 않는 HTTP Method 입니다."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleEtc(Exception ex, HttpServletRequest req) {
		log.error("[ETC] uri={} msg={}", req.getRequestURI(), ex.getMessage(), ex);
		return ResponseEntity
			.status(CommonCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ApiResponse.fail(CommonCode.INTERNAL_SERVER_ERROR, CommonCode.INTERNAL_SERVER_ERROR.getMessage()));
	}

	@ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
		org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
		String msg = "파라미터 형식 오류: " + ex.getName() +
			" (요구 타입=" + (ex.getRequiredType()!=null?ex.getRequiredType().getSimpleName():"") + ")";
		log.warn("[TYPE_MISMATCH] uri={} msg={}", req.getRequestURI(), msg);
		return ResponseEntity.status(CommonCode.BAD_REQUEST.getHttpStatus())
			.body(ApiResponse.fail(CommonCode.BAD_REQUEST, msg));
	}

	@ExceptionHandler(org.springframework.validation.BindException.class)
	public ResponseEntity<ApiResponse<Void>> handleBind(org.springframework.validation.BindException ex, HttpServletRequest req) {
		String msg = ex.getBindingResult().getFieldErrors().stream()
			.map(fe -> fe.getField() + "=" + fe.getDefaultMessage())
			.findFirst().orElse(CommonCode.BAD_REQUEST.getMessage());
		log.warn("[BIND] uri={} msg={}", req.getRequestURI(), msg);
		return ResponseEntity.status(CommonCode.BAD_REQUEST.getHttpStatus())
			.body(ApiResponse.fail(CommonCode.BAD_REQUEST, msg));
	}

	// 4xx/5xx 응답(바디 확인 가능)
	@ExceptionHandler(RestClientResponseException.class)
	public ResponseEntity<ApiResponse<Void>> handleRestClientResp(
		RestClientResponseException ex, HttpServletRequest req) {

		log.error("[REST_CLIENT] uri={} status={} body={}",
			req.getRequestURI(), ex.getRawStatusCode(), ex.getResponseBodyAsString(), ex);

		CommonCode code = switch (ex.getRawStatusCode()) {
			case 400 -> CommonCode.BAD_REQUEST;
			case 401 -> CommonCode.UNAUTHORIZED;
			case 403 -> CommonCode.FORBIDDEN;
			case 404 -> CommonCode.NOT_FOUND;
			default -> CommonCode.INTERNAL_SERVER_ERROR; // 5xx 포함
		};

		String msg = "외부 서비스 호출 중 오류가 발생했습니다. (status=" + ex.getRawStatusCode() + ")";
		return ResponseEntity.status(code.getHttpStatus())
			.body(ApiResponse.fail(code, msg));
	}

	@ExceptionHandler(ResourceAccessException.class)
	public ResponseEntity<ApiResponse<Void>> handleResourceAccess(
		ResourceAccessException ex, HttpServletRequest req) {
		log.error("[REST_IO] uri={} msg={}", req.getRequestURI(), ex.getMessage(), ex);
		return ResponseEntity.status(CommonCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ApiResponse.fail(CommonCode.INTERNAL_SERVER_ERROR, "외부 서비스 연결 실패. 잠시 후 재시도해주세요."));
	}
}
