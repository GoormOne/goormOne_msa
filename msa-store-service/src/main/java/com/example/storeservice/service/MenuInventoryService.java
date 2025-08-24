package com.example.storeservice.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.storeservice.repository.MenuInventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuInventoryService {

	private final MenuInventoryRepository menuInventoryRepository;

	@Transactional
	public int getAvailableQty(UUID menuId) {
		var inventory = menuInventoryRepository.findById(menuId)
			.orElse
	}
}
