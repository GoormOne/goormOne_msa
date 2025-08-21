package com.example.msaorderservice.cart.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemRes {
	private UUID menuId;
	private String menuName;
	private int menuPrice;
	private int quantity;
	private int lineTotal;
}
