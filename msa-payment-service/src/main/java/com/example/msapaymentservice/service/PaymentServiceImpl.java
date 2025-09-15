package com.example.msapaymentservice.service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.common.dto.OrderCheckoutView;
import com.example.common.entity.PaymentStatus;
import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;
import com.example.msapaymentservice.client.OrderClient;
import com.example.msapaymentservice.client.StoreClient;
import com.example.msapaymentservice.client.TossPaymentClient;
import com.example.msapaymentservice.dto.LatestPendingOrderRes;
import com.example.msapaymentservice.dto.PaymentSearchRes;
import com.example.msapaymentservice.dto.StoreClientRes;
import com.example.msapaymentservice.dto.TossPaymentRes;
import com.example.msapaymentservice.entity.PaymentAuditEntity;
import com.example.msapaymentservice.entity.PaymentEntity;
import com.example.msapaymentservice.kafka.producer.PaymentEventsPublisher;
import com.example.msapaymentservice.repository.PaymentAuditRepository;
import com.example.msapaymentservice.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final OrderClient orderClient;
	private final StoreClient storeClient;
	private final PaymentRepository paymentRepository;
	private final TossPaymentClient tossPaymentClient;
	private final PaymentAuditRepository paymentAuditRepository;
	private final PaymentEventsPublisher paymentEventsPublisher;

	@Value("${payments.redirect-base-url}")
	private String redirectBaseUrl;

	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<Void> redirectToCheckout(UUID customerId) {

		LatestPendingOrderRes latest = orderClient.getLatestPendingOrder(customerId);
		UUID orderId = latest.getOrderId();

		orderClient.getCheckout(orderId, customerId);

		String url = UriComponentsBuilder.fromHttpUrl(redirectBaseUrl)
			.queryParam("orderId", orderId)
			.queryParam("customerId", customerId)
			.toUriString();

		return ResponseEntity.status(HttpStatus.SEE_OTHER)
			.location(URI.create(url))
			.build();
	}

	@Override
	@Transactional(readOnly = true)
	public OrderCheckoutView getCheckout(UUID customerId, UUID orderId) {
		log.info(CommonCode.ORDER_SEARCH.getMessage());
		return orderClient.getCheckout(orderId, customerId);
	}

	@Override
	@Transactional
	public ResponseEntity<String> cancelPayment(UUID customerId, String paymentKey, String cancelReason) {

		PaymentEntity payment = paymentRepository.findByPaymentKey(paymentKey);


		if ("REFUNDED".equalsIgnoreCase(payment.getStatus())) {
			orderClient.updateOrderStatus(payment.getOrderId(), null, PaymentStatus.REFUNDED);
			return ResponseEntity.ok("{\"message\":\"already canceled\"}");
		}


		String tossRaw = tossPaymentClient.cancelPayment(customerId, paymentKey, cancelReason);


		payment.setStatus("REFUNDED");
		paymentRepository.save(payment);

		paymentAuditRepository.findById(payment.getPaymentId())
				.ifPresentOrElse(audit ->{
					audit.setUpdatedAt(OffsetDateTime.now());
					audit.setUpdatedBy(customerId);
				}, () -> {});

		orderClient.updateOrderStatus(payment.getOrderId(), customerId, PaymentStatus.REFUNDED);

		log.info(CommonCode.PAYMENT_CANCEL_SUCCESS.getMessage());

		return ResponseEntity.ok(tossRaw);
	}

	@Override
	public Page<PaymentSearchRes> getMyPayments(UUID customerId, Pageable pageable) {

		List<UUID> orderIds = orderClient.findOrderIdsByCustomer(customerId);
		if (orderIds == null || orderIds.isEmpty()) {
			return Page.empty(pageable);
		}

		log.info(CommonCode.PAYMENT_SEARCH_SUCCESS.getMessage());

		return paymentRepository.findByOrderIdIn(orderIds, pageable)
			.map(this::toSummary);
	}

	@Override
	public Page<PaymentSearchRes> getOwnerStorePayments(UUID ownerId, UUID storeId, Pageable pageable) {

		StoreClientRes store = storeClient.getStoreDetail(storeId);
		log.info("storeId={}, headerOwnerId={}, store.ownerId={}",
			storeId, ownerId, store.getOwnerId());

		if (store.getOwnerId() == null || !store.getOwnerId().equals(ownerId)) {
			throw new BusinessException(CommonCode.STORE_AUTH_FAIL);
		}


		List<UUID> orderIds = orderClient.findOrderIdsByStore(ownerId, storeId);
		if (orderIds == null || orderIds.isEmpty()) {
			return Page.empty(pageable);
		}

		log.info(CommonCode.PAYMENT_SEARCH_SUCCESS.getMessage());

		return paymentRepository.findByOrderIdIn(orderIds, pageable)
			.map(this::toSummary);
	}

	private PaymentSearchRes toSummary(PaymentEntity payment) {
		return PaymentSearchRes.builder()
			.paymentId(payment.getPaymentId())
			.orderId(payment.getOrderId())
			.status(payment.getStatus())
			.amount(payment.getPaymentAmount())
			.currency(payment.getCurrency())
			.method(payment.getPaymentMethod())
			.cardCompany(payment.getCardCompany())
			.cardLast4(payment.getCardLast4())
			.requestedAt(payment.getRequestedAt())
			.approvedAt(payment.getApprovedAt())
			.receiptUrl(payment.getReceiptUrl())
			.build();
	}

	@Override
	@Transactional
	public void handleSuccess(UUID customerId, UUID orderId, String paymentKey, int amount) {

		var checkout = orderClient.getCheckout(orderId, customerId);

		if (checkout.getAmount() != amount) {
			PaymentEntity fail = PaymentEntity.builder()
				.orderId(orderId)
				.paymentKey(paymentKey)
				.status("FAILED")
				.paymentAmount(amount)
				.currency("KRW")
				.failureCode("AMOUNT_MISMATCH")
				.failureReason("amount mismatch")
				.requestedAt(OffsetDateTime.now())
				.build();
			paymentRepository.save(fail);

			orderClient.updateOrderStatus(orderId, customerId, PaymentStatus.FAILED);

			return;
		}

		TossPaymentRes confirm = tossPaymentClient.confirmPayment(paymentKey, orderId, amount);

		PaymentEntity ok = PaymentEntity.builder()
			.orderId(orderId)
			.paymentKey(paymentKey)
			.status(confirm.getStatus())
			.paymentMethod(confirm.getMethod())
			.cardCompany(confirm.getCard() != null ? confirm.getCard().getCompany() : null)
			.cardBin(confirm.getCard() != null ? confirm.getCard().getBin() : null)
			.cardLast4(confirm.getCard() != null ? confirm.getCard().getLast4() : null)
			.cardNumberMasked(confirm.getCard() != null ? confirm.getCard().getNumber() : null)
			.paymentAmount(confirm.getTotalAmount())
			.currency(confirm.getCurrency() != null ? confirm.getCurrency() : "KRW")
			.requestedAt(confirm.getRequestedAt())
			.approvedAt(confirm.getApprovedAt())
			.receiptUrl(confirm.getReceipt() != null ? confirm.getReceipt().getUrl() : null)
			.approveNo(confirm.getApproveNo())
			.issuerCode(confirm.getCard() != null ? confirm.getCard().getIssuerCode() : null)
			.acquirerCode(confirm.getCard() != null ? confirm.getCard().getAcquirerCode() : null)
			.isPartialCancelable(Boolean.TRUE)
			.mId(confirm.getMId())
			.lastTransactionKey(confirm.getLastTransactionKey())
			.build();
		paymentRepository.save(ok);

		PaymentAuditEntity audit = PaymentAuditEntity.builder()
			.payment(ok)
			.createdAt(OffsetDateTime.now())
			.createdBy(customerId)
			.build();
		paymentAuditRepository.save(audit);

		orderClient.updateOrderStatus(orderId, customerId, PaymentStatus.PAID);

		log.info(CommonCode.PAYMENT_COMPLETE.getMessage());

		paymentEventsPublisher.paymentSuccess(
			orderId.toString(),
			new com.example.msapaymentservice.dto.PaymentSuccessRes(orderId, paymentKey, amount),
			orderId.toString(),   // correlationId
			null                  // causationId
		);
	}

	@Override
	@Transactional
	public void handleFail(UUID customerId, UUID orderId, String errorCode, String errorMsg) {
		PaymentEntity fail = PaymentEntity.builder()
			.orderId(orderId)
			.status("FAILED")
			.paymentAmount(0)
			.currency("KRW")
			.failureCode(errorCode)
			.failureReason(errorMsg)
			.requestedAt(OffsetDateTime.now())
			.build();
		paymentRepository.save(fail);

		PaymentAuditEntity audit = PaymentAuditEntity.builder()
			.payment(fail)
			.createdAt(OffsetDateTime.now())
			.createdBy(customerId)
			.build();
		paymentAuditRepository.save(audit);

		orderClient.updateOrderStatus(orderId, customerId, PaymentStatus.FAILED);

		log.info(CommonCode.PAYMENT_FAILED.getMessage());
	}
}
