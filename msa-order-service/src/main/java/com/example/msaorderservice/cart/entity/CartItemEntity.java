package com.example.msaorderservice.cart.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "p_cart_items")
public class CartItemEntity {
	@Id
	@GeneratedValue
	@org.hibernate.annotations.UuidGenerator
	@Column(name = "cart_item_id")
	private UUID cartItemId;

	@Column(name = "cart_id", nullable = false)
	private UUID cartId;

	@Column(name = "menu_id", nullable = false)
	private UUID menuId;

	@Column(name = "quantity", nullable = false)
	private int quantity;
}
