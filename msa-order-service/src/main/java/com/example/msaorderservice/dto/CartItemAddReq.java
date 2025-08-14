package com.example.msaorderservice.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemAddReq {
	private UUID storeId;
	private UUID userId;
	private UUID menuId;
	private int quantity;
}
