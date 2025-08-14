package com.example.msaorderservice.dto;

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
public class CartItemsPageRes {
	private UUID storeId;
	private int page;
	private int size;
	private long totalItems;
	private int totalPages;
	private int pageTotalPrice;
	private List<CartItemRes> items;
}
