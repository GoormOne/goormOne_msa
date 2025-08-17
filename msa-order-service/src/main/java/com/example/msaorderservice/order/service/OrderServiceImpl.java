package com.example.msaorderservice.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.msaorderservice.cart.dto.MenuLookUp;
import com.example.msaorderservice.cart.entity.CartEntity;
import com.example.msaorderservice.cart.entity.CartItemEntity;
import com.example.msaorderservice.order.dto.OrderCreateReq;
import com.example.msaorderservice.order.dto.OrderCreateRes;
import com.example.msaorderservice.order.dto.OrderLineRes;
import com.example.msaorderservice.order.entity.OrderEntity;
import com.example.msaorderservice.order.entity.OrderItemEntity;
import com.example.msaorderservice.order.entity.OrderStatus;
import com.example.msaorderservice.order.entity.PaymentStatus;
import com.example.msaorderservice.order.repository.OrderItemRepository;
import com.example.msaorderservice.order.repository.OrderRepository;
import com.example.msaorderservice.cart.repository.CartItemRepository;
import com.example.msaorderservice.cart.repository.CartRepository;
import com.example.msaorderservice.cart.service.MenuClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final MenuClient menuClient;

	@Override
	@Transactional
	public OrderCreateRes createOrder(UUID customerId, OrderCreateReq req) {
		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new IllegalStateException("장바구니가 없습니다."));
		var cartItems = cartItemRepository.findByCartId(cart.getCartId(), Pageable.unpaged()).getContent();
		if (cartItems.isEmpty())
			throw new IllegalStateException("장바구니가 비어 있습니다.");

		UUID storeId = cart.getStoreId();

		OrderEntity order = new OrderEntity();
		order.setCustomerId(customerId);
		order.setStoreId(storeId);
		order.setAddressId(req.getAddressId());
		order.setRequestMessage(req.getRequestMessage());
		order.setOrderStatus(OrderStatus.PENDING);
		order.setPaymentStatus(PaymentStatus.PENDING);

		List<OrderItemEntity> toSave = new ArrayList<>();
		List<OrderLineRes> lines = new ArrayList<>();

		int total = 0;
		for (CartItemEntity ci : cartItems) {
			MenuLookUp menu = menuClient.getMenuDetail(ci.getMenuId());

			int unitPrice = menu.getMenuPrice();
			int qty = ci.getQuantity();

			OrderItemEntity oi = OrderItemEntity.builder()
				.orderId(order)
				.menuId(ci.getMenuId())
				.menuName(menu.getMenuName())
				.menuPrice(unitPrice)
				.quantity(qty)
				.lineTotal(unitPrice * qty)
				.build();

			toSave.add(oi);

			total += oi.getLineTotal();
			lines.add(new OrderLineRes(
				ci.getMenuId(),
				menu.getMenuName(),
				qty,
				unitPrice
			));
		}
		order.setTotalPrice(total);

		order = orderRepository.save(order);
		OrderEntity savedOrder = order;
		toSave.forEach(i -> i.setOrderId(savedOrder));
		orderItemRepository.saveAll(toSave);

		cartItemRepository.deleteByCartId(cart.getCartId());

		return new OrderCreateRes(
			order.getOrderId(),
			storeId,
			total,
			order.getOrderStatus(),
			order.getPaymentStatus(),
			lines
		);
	}
}
