package com.example.msapaymentservice.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreClientRes {
	private UUID storeId;
	private UUID ownerId;
	private String storeName;
}
