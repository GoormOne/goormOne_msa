package com.example.msaorderservice.order.dto;

import java.util.UUID;

public class OrderLineRes {
	private UUID menuId;
	private String menuName;
	private int quantity;
	private int menuPrice;
	private int lineTotal;

	public OrderLineRes() {}

	public OrderLineRes(UUID menuId, String menuName, int quantity, int menuPrice) {
		this.menuId = menuId;
		this.menuName = menuName;
		this.quantity = quantity;
		this.menuPrice = menuPrice;
		this.lineTotal = menuPrice * quantity;
	}

	public UUID getMenuId() { return menuId; }
	public void setMenuId(UUID menuId) { this.menuId = menuId; }

	public String getMenuName() { return menuName; }
	public void setMenuName(String menuName) { this.menuName = menuName; }

	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) { this.quantity = quantity; }

	public int getUnitPrice() { return menuPrice; }
	public void setUnitPrice(int unitPrice) {
		this.menuPrice = unitPrice;
		this.lineTotal = unitPrice * this.quantity; // 일관성 유지
	}

	public int getLineTotal() {return lineTotal;}
	public void setLineTotal(int lineTotal) {this.lineTotal = lineTotal;}

}
