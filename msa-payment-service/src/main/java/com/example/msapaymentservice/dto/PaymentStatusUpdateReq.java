package com.example.msapaymentservice.dto;

import com.example.common.entity.PaymentStatus;

public class PaymentStatusUpdateReq {
	private PaymentStatus paymentStatus;
	public PaymentStatus getPaymentStatus() { return paymentStatus; }
	public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
}
