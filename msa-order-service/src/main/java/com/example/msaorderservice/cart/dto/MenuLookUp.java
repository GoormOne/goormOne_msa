package com.example.msaorderservice.cart.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuLookUp {
	@JsonProperty("menu_id") private UUID menuId;
	@JsonProperty("store_id") private UUID storeId;
	@JsonProperty("menu_name") private String menuName;
	@JsonProperty("menu_price") private int menuPrice;
}
