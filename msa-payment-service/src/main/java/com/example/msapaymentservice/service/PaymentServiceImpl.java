package com.example.msapaymentservice.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.common.dto.OrderCheckoutView;
import com.example.common.entity.PaymentStatus;
import com.example.msapaymentservice.client.OrderClient;
import com.example.msapaymentservice.client.TossPaymentClient;
import com.example.msapaymentservice.dto.TossPaymentRes;
import com.example.msapaymentservice.entity.PaymentEntity;
import com.example.msapaymentservice.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final OrderClient orderClient;
	private final PaymentRepository paymentRepository;
	private final TossPaymentClient tossPaymentClient;

	@Override
	@Transactional(readOnly = true)
	public OrderCheckoutView getCheckout(UUID customerId, UUID orderId) {
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

		orderClient.updateOrderStatus(payment.getOrderId(), customerId, PaymentStatus.REFUNDED);


		return ResponseEntity.ok(tossRaw);
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

		orderClient.updateOrderStatus(orderId, customerId, PaymentStatus.PAID);
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

		orderClient.updateOrderStatus(orderId, customerId, PaymentStatus.FAILED);
	}
}
