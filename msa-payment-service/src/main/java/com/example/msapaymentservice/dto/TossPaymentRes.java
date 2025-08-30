package com.example.msapaymentservice.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentRes {
	private String status;
	private String method;
	private String currency;
	private int totalAmount;
	private OffsetDateTime requestedAt;
	private OffsetDateTime approvedAt;
	private String approveNo;
	private String mId;
	private String lastTransactionKey;

	private Receipt receipt;
	private Card card;

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Receipt {
		private String url;
	}

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Card {
		private String company;
		private String bin;
		private String last4;
		private String number;
		private String issuerCode;
		private String acquirerCode;
	}
}
