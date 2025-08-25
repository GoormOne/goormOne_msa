package com.example.storeservice.controller;

import java.util.UUID;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.common.dto.ApiResponse;
import com.example.storeservice.service.MenuInventoryService;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/inventory")
@RequiredArgsConstructor
@Validated
public class MenuInventoryController {

	private final MenuInventoryService inventoryService;

	@PostMapping("/{menuId}/init")
	public ApiResponse<Void> init(@PathVariable UUID menuId,
		@RequestParam @Min(1) int initialQty,
		@RequestParam(defaultValue = "false") boolean infinite) {

		inventoryService.initInventory(menuId, initialQty, infinite);
		return ApiResponse.success();
	}

	@GetMapping("/{menuId}/available")
	public ApiResponse<Integer> getAvailable(@PathVariable UUID menuId) {
		int qty = inventoryService.getAvailableQty(menuId);
		return ApiResponse.success(qty);
	}

	@GetMapping("/{menuId}/sold-out")
	public ApiResponse<Boolean> isSoldOut(@PathVariable UUID menuId) {
		boolean soldOut = inventoryService.isSoldOut(menuId);
		return ApiResponse.success(soldOut);
	}

	@PostMapping("/{menuId}/reserve")
	public ApiResponse<Void> reserve(@PathVariable UUID menuId,
		@RequestParam @Min(1) int qty) {
		inventoryService.reserve(menuId, qty);
		return ApiResponse.success();
	}

	@PostMapping("/{menuId}/confirm")
	public ApiResponse<Void> confirm(@PathVariable UUID menuId,
		@RequestParam @Min(1) int qty) {
		inventoryService.confirm(menuId, qty);
		return ApiResponse.success();
	}

	@PostMapping("/{menuId}/release")
	public ApiResponse<Void> release(@PathVariable UUID menuId,
		@RequestParam @Min(1) int qty) {
		inventoryService.release(menuId, qty);
		return ApiResponse.success();
	}

	@PostMapping("/{menuId}/adjust")
	public ApiResponse<Void> adjust(@PathVariable UUID menuId,
		@RequestParam @Min(0) int qty) {
		inventoryService.adjust(menuId, qty);
		return ApiResponse.success();
	}

	@PostMapping("/{menuId}/infinite")
	public ApiResponse<Void> setInfinite(@PathVariable UUID menuId,
		@RequestParam("value") boolean infinite) {
		inventoryService.setInfinite(menuId, infinite);
		return ApiResponse.success();
	}
}
