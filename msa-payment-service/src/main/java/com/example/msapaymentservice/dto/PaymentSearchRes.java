package com.example.msapaymentservice.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSearchRes {
	private UUID paymentId;
	private UUID orderId;
	private String status;
	private Integer amount;
	private String currency;
	private String method;
	private String cardCompany;
	private String cardLast4;
	private OffsetDateTime requestedAt;
	private OffsetDateTime approvedAt;
	private String receiptUrl;
}
