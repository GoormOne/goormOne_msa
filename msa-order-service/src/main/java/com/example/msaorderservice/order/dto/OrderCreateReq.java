package com.example.msaorderservice.order.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OrderCreateReq {

	@NotNull
	private UUID addressId;

	@Size(max = 2000)
	private String requestMessage;

	public UUID getAddressId() {return addressId;}
	public void setAddressId(UUID addressId) {this.addressId = addressId;}

	public String getRequestMessage() {return requestMessage;}
	public void setRequestMessage(String requestMessage) {this.requestMessage = requestMessage;}
}
