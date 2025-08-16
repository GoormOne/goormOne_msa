package com.example.msaorderservice.cart.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "p_carts")
@org.hibernate.annotations.DynamicInsert
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CartEntity {

	@Id
	@Column(name = "cart_id", columnDefinition = "uuid", updatable = false, nullable = false)
	@org.hibernate.annotations.Generated(org.hibernate.annotations.GenerationTime.INSERT)
	private UUID cartId;

	@Column(name = "customer_id", nullable = false, columnDefinition = "uuid")
	private UUID customerId;

	@Column(name = "store_id", nullable = false, columnDefinition = "uuid")
	private UUID storeId;
}
