package com.example.msaorderservice.order.entity;

import java.util.UUID;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "p_order_items")
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEntity {

	@Id
	@org.hibernate.annotations.Generated(org.hibernate.annotations.GenerationTime.INSERT)
	@Column(name = "order_item_id", columnDefinition = "uuid", nullable = false)
	private UUID orderItemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", columnDefinition = "uuid", nullable = false)
	private OrderEntity orderId;

	@Column(name = "menu_id", columnDefinition = "uuid", nullable = false)
	private UUID menuId;

	@Column(name = "menu_name", nullable = false)
	private String menuName;

	@Column(name = "menu_price", nullable = false)
	private int menuPrice;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Column(name = "line_total", nullable = false)
	private int lineTotal;
}
