package com.example.msapaymentservice.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.common.dto.OrderCheckoutView;
import com.example.msapaymentservice.dto.PaymentSearchRes;
import com.example.msapaymentservice.dto.TossCancelReq;
import com.example.msapaymentservice.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class paymentController {

	private final PaymentService paymentService;

	@GetMapping("/checkout")
	public ResponseEntity<?> checkout(
		@RequestHeader(value = "X-User-Id", required = false) UUID customerIdHeader,
		@RequestParam(value = "customerId", required = false) UUID customerIdParam,
		@RequestParam(value = "orderId", required = false) UUID orderId
	) {
		UUID customerId = (customerIdHeader != null) ? customerIdHeader : customerIdParam;
		if (customerId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");

		if (orderId != null) {
			return ResponseEntity.ok(paymentService.getCheckout(customerId, orderId));
		}

		return paymentService.redirectToCheckout(customerId);
	}

	@PostMapping("/{paymentKey}/cancel")
	public ResponseEntity<String> cancelPayment(
		@RequestHeader(value = "X-User-Id", required = false) String xUserId,
		@PathVariable String paymentKey,
		@RequestBody(required = false) TossCancelReq req
	) {
		UUID customerId = null;
		if (xUserId != null && !xUserId.isBlank()) {
			try {
				customerId = UUID.fromString(xUserId.trim().replace("\"", ""));
			} catch (IllegalArgumentException ex) {
				return ResponseEntity.badRequest()
					.body("{\"error\":\"X-User-Id must be a UUID\"}");
			}
		}

		String reason = (req == null || req.getCancelReason() == null || req.getCancelReason().isBlank())
			? "USER_REQUEST"
			: req.getCancelReason().trim();

		return paymentService.cancelPayment(customerId, paymentKey, reason);
	}


	@GetMapping("/me")
	public Page<PaymentSearchRes> getMyPayments(
		@RequestHeader("X-User-Id") UUID customerId,
		@PageableDefault(size = 20, sort = "approvedAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {
		return paymentService.getMyPayments(customerId, pageable);
	}

	@GetMapping("/store/{storeId}")
	public Page<PaymentSearchRes> getOwnerStorePayments(
		@RequestHeader("X-User-Id") UUID ownerId,
		@PathVariable UUID storeId,
		@PageableDefault(size = 20, sort = "approvedAt", direction = Sort.Direction.DESC) Pageable pageable
	) {

		return paymentService.getOwnerStorePayments(ownerId, storeId, pageable);
	}

	@GetMapping("/success")
	public ResponseEntity<Void> success(
		@RequestHeader(value = "X-User-Id", required = false) UUID userId,
		@RequestParam(required = false) UUID customerId,
		@RequestParam String paymentKey,
		@RequestParam UUID orderId,
		@RequestParam int amount
	) {
		UUID targetCustomerId = (userId != null) ? userId : customerId;
		paymentService.handleSuccess(targetCustomerId, orderId, paymentKey, amount);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/fail")
	public ResponseEntity<Void> fail(
		@RequestHeader(value = "X-User-Id", required = false) UUID userId,
		@RequestParam(required = false) UUID customerId,
		@RequestParam UUID orderId,
		@RequestParam String code,
		@RequestParam String message
	) {
		UUID targetCustomerId = (userId != null) ? userId : customerId;
		paymentService.handleFail(targetCustomerId, orderId, code, message);
		return ResponseEntity.noContent().build();
	}
}
