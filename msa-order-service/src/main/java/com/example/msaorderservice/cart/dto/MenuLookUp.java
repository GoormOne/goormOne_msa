package com.example.msaorderservice.cart.dto;

import java.util.UUID;

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
public class MenuLookUp {
	private UUID menuId;
	private String menuName;
	private int menuPrice;
}
