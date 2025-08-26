package com.example.storeservice.service;

import static java.awt.SystemColor.*;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.storeservice.entity.Menu;
import com.example.storeservice.entity.MenuInventory;
import com.example.storeservice.exception.InvalidQuantityException;
import com.example.storeservice.exception.InventoryNotFoundException;
import com.example.storeservice.exception.OutOfStockException;
import com.example.storeservice.exception.ReservedNotEnoughException;
import com.example.storeservice.repository.MenuInventoryRepository;
import com.example.storeservice.repository.MenuRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuInventoryService {

	private final MenuInventoryRepository inventoryRepo;
	private final MenuRepository menuRepo;
	private final PlatformTransactionManager transactionManager;

	private static final int MAX_ATTEMPTS = 5;
	private static final long BASE_BACKOFF_MS = 15;
	private TransactionTemplate transactionTemplate;

	@PostConstruct
	void init() {
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Transactional(readOnly = true)
	public int getAvailableQty(UUID menuId) {
		MenuInventory inventory = inventoryRepo.findById(menuId)
			.orElseThrow(InventoryNotFoundException::new);
		return inventory.getAvailableQty();
	}

	@Transactional(readOnly = true)
	public boolean isSoldOut(UUID menuId) {
		MenuInventory inventory = inventoryRepo.findById(menuId)
			.orElseThrow(InventoryNotFoundException::new);
		if (inventory.isInfiniteStock()) return false;
		return inventory.getAvailableQty() <= 0;
	}

	public void initInventory(UUID menuId, int initialQty, boolean infinite) {
		if (inventoryRepo.existsById(menuId)) {
			return;
		}

	  Menu menu = menuRepo.findById(menuId).orElseThrow(InventoryNotFoundException::new);

		if (initialQty <= 0) throw new InvalidQuantityException("초기 수량은 1개 이상 설정 가능합니다.");

		MenuInventory inventory = new MenuInventory(
			menuId,
			menu,
			infinite,
			infinite ? 0 : initialQty,
			0,
			null
		);
		inventoryRepo.save(inventory);
	}

	public void reserve(UUID menuId, int qty) {
		validateQty(qty);

		runWithRetry(() -> transactionTemplate.executeWithoutResult(status -> {
			MenuInventory inv = inventoryRepo.findById(menuId).orElseThrow(InventoryNotFoundException::new);
			if (inv.isInfiniteStock()) return;
			if (inv.getAvailableQty() < qty) throw new OutOfStockException();

			inv.setAvailableQty(inv.getAvailableQty() - qty);
			inv.setReservedQty(inv.getReservedQty() + qty);
		}));
	}

	public void confirm(UUID menuId, int qty) {
		validateQty(qty);

		runWithRetry(() -> transactionTemplate.executeWithoutResult(status -> {
			MenuInventory inv = inventoryRepo.findById(menuId).orElseThrow(InventoryNotFoundException::new);
			if (inv.isInfiniteStock()) return;
			if (inv.getReservedQty() < qty) throw new ReservedNotEnoughException();

			inv.setReservedQty(inv.getReservedQty() - qty);
		}));
	}

	public void release(UUID menuId, int qty) {
		validateQty(qty);

		runWithRetry(() -> transactionTemplate.executeWithoutResult(status -> {
			MenuInventory inv = inventoryRepo.findById(menuId).orElseThrow(InventoryNotFoundException::new);

			if (inv.isInfiniteStock()) return;
			if (inv.getReservedQty() < qty) throw new ReservedNotEnoughException();

			inv.setReservedQty(inv.getReservedQty() - qty);
			inv.setAvailableQty(inv.getAvailableQty() + qty);
		}));
	}

	public void adjust(UUID menuId, int newAvailableQty) {
		if (newAvailableQty < 0) throw new InvalidQuantityException("주문 가능 수량은 1 이상이어야 가능합니다.");

		runWithRetry(() -> transactionTemplate.executeWithoutResult(status -> {
			MenuInventory inv = inventoryRepo.findById(menuId).orElseThrow(InventoryNotFoundException::new);
			if (inv.isInfiniteStock()) return;

			inv.setAvailableQty(newAvailableQty);
		}));
	}

	public void setInfinite(UUID menuId, boolean infinite) {
		runWithRetry(() -> transactionTemplate.executeWithoutResult(status -> {
			MenuInventory inv = inventoryRepo.findById(menuId).orElseThrow(InventoryNotFoundException::new);
			inv.setInfiniteStock(infinite);
			if (infinite) {
				inv.setAvailableQty(0);
				inv.setReservedQty(0);
			}
		}));
	}

	private void runWithRetry(Runnable action) {
		int tries = 0;
		while (true) {
			try {
				action.run();
				return;
			} catch (OptimisticLockException
					 | ObjectOptimisticLockingFailureException
					 | StaleObjectStateException e) {
				tries++;
				if (tries >= MAX_ATTEMPTS) throw e;
				sleepBackoff(tries);
			}
		}
	}

	private void sleepBackoff(int attempt) {
		long jitter = ThreadLocalRandom.current().nextLong(0, 8);
		long backoff = (long)(BASE_BACKOFF_MS * Math.pow(2, attempt - 1)) + jitter;
		try {Thread.sleep(backoff);} catch (InterruptedException ie) {Thread.currentThread().interrupt();}
	}

	private void validateQty(int qty) {
		if (qty <= 0) throw new InvalidQuantityException("qty must be > 0");
	}
}
