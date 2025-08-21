package com.example.common.dto;



import com.example.common.entity.PaymentStatus;

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
public class PaymentStatusUpdatedReq {
	private PaymentStatus paymentStatus;
	private String reason;
}
