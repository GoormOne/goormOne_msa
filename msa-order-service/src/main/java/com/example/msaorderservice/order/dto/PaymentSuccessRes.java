package com.example.msaorderservice.order.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessRes {
	private UUID orderId;
	private String paymentKey;
	private int amount;
}
