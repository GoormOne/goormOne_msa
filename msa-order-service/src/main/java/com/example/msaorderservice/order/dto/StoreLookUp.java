package com.example.msaorderservice.order.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreLookUp {
	private UUID storeId;
	private UUID ownerId;
	private String storeName;
}
