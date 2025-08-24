package com.example.storeservice.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "p_menu_inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuInventory {

	@Id
	@Column(name = "menu_id", nullable = false)
	private UUID menuId;

	@OneToOne
	@MapsId
	@JoinColumn(name = "menu_id")
	private Menu menu;

	@Column(name = "is_infinite_stock", nullable = false)
	private boolean isInfiniteStock;

	@Column(name = "available_qty", nullable = false)
	private int availableQty;

	@Column(name = "reserved_qty", nullable = false)
	private int reservedQty;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	public boolean isSoldOut() {
		if (Boolean.TRUE.equals(isInfiniteStock)) return false;
		return reservedQty <= 0;
	}
}
