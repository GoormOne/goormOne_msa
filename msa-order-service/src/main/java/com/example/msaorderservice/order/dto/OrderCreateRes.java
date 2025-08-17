package com.example.msaorderservice.order.dto;

import java.util.List;
import java.util.UUID;

import com.example.msaorderservice.order.entity.OrderStatus;
import com.example.msaorderservice.order.entity.PaymentStatus;

public class OrderCreateRes {
	private UUID orderId;
	private UUID storeId;
	private int totalPrice;
	private OrderStatus orderStatus;
	private PaymentStatus paymentStatus;
	private List<OrderLineRes> items;


	public OrderCreateRes() {}

	public OrderCreateRes(UUID orderId, UUID storeId, int totalPrice,
		OrderStatus orderStatus, PaymentStatus paymentStatus,
		List<OrderLineRes> items) {
		this.orderId = orderId;
		this.storeId = storeId;
		this.totalPrice = totalPrice;
		this.orderStatus = orderStatus;
		this.paymentStatus = paymentStatus;
		this.items = items;
	}

	public UUID getOrderId() { return orderId; }
	public void setOrderId(UUID orderId) { this.orderId = orderId; }

	public UUID getStoreId() { return storeId; }
	public void setStoreId(UUID storeId) { this.storeId = storeId; }

	public int getTotalPrice() { return totalPrice; }
	public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }

	public OrderStatus getOrderStatus() { return orderStatus; }
	public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

	public PaymentStatus getPaymentStatus() { return paymentStatus; }
	public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

	public List<OrderLineRes> getItems() { return items; }
	public void setItems(List<OrderLineRes> items) { this.items = items; }

}
