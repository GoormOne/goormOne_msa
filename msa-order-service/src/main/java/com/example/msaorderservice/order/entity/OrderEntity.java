package com.example.msaorderservice.order.entity;

import java.util.UUID;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.common.entity.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "p_orders")
@DynamicInsert
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OrderEntity {

	@Id
	@Column(name = "order_id", columnDefinition = "uuid", updatable = false, nullable = false)
	@org.hibernate.annotations.Generated(org.hibernate.annotations.GenerationTime.INSERT)
	private UUID orderId;

	@Column(name = "customer_id", columnDefinition = "uuid", nullable = false)
	private UUID customerId;

	@Column(name = "store_id", columnDefinition = "uuid", nullable = false)
	private UUID storeId;

	@Column(name = "address_id", columnDefinition = "uuid", nullable = false)
	private UUID addressId;

	@Column(name = "total_price", nullable = false)
	private int totalPrice;

	@Column(name = "request_message")
	private String requestMessage;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "order_status", columnDefinition = "order_status", nullable = false)
	private OrderStatus orderStatus = OrderStatus.PENDING;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "payment_status", columnDefinition = "payment_status", nullable = false)
	private PaymentStatus paymentStatus = PaymentStatus.PENDING;
}
