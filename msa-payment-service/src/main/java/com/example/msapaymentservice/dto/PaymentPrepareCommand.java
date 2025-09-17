package com.example.msapaymentservice.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class PaymentPrepareCommand {
	@JsonProperty("orderId")
	@JsonAlias({"order_id"})
	private UUID orderId;

	@JsonProperty("customerId")
	@JsonAlias({"customer_id"})
	private UUID customerId;

	@JsonProperty("amount")
	private int amount;
}
