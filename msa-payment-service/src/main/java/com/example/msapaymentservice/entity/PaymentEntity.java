package com.example.msapaymentservice.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "p_payments")
@Getter
@Setter
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

	@Id
	@Column(name = "payment_id", columnDefinition = "uuid", updatable = false, nullable = false)
	@org.hibernate.annotations.Generated(org.hibernate.annotations.GenerationTime.INSERT)
	private UUID paymentId;

	@Column(name = "order_id", nullable = false)
	private UUID orderId;

	@Column(name = "payment_key", nullable = false, length = 100, unique = true)
	private String paymentKey;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "payment_method", nullable = false, length = 20)
	private String paymentMethod;

	@Column(name = "card_company", length = 30)
	private String cardCompany;

	@Column(name = "card_bin", length = 8)
	private String cardBin;

	@Column(name = "card_last4", length = 4)
	private String cardLast4;

	@Column(name = "card_number_masked", length = 25)
	private String cardNumberMasked;

	@Column(name = "payment_amount", nullable = false)
	private int paymentAmount;

	@Column(name = "currency", nullable = false, length = 3)
	@Builder.Default
	private String currency = "KRW";

	@Column(name = "requested_at")
	private OffsetDateTime requestedAt;

	@Column(name = "approved_at")
	private OffsetDateTime approvedAt;

	@Column(name = "receipt_url", length = 300)
	private String receiptUrl;

	@Column(name = "approve_no", length = 32)
	private String approveNo;

	@Column(name = "issuer_code", length = 8)
	private String issuerCode;

	@Column(name = "acquirer_code", length = 8)
	private String acquirerCode;

	@Column(name = "is_partial_cancelable", nullable = false)
	@Builder.Default
	private Boolean isPartialCancelable = false;

	@Column(name = "failure_reason")
	private String failureReason;

	@Column(name = "failure_code", length = 50)
	private String failureCode;

	@Column(name = "m_id", length = 50)
	private String mId;

	@Column(name = "last_transaction_key", length = 64)
	private String lastTransactionKey;
}
