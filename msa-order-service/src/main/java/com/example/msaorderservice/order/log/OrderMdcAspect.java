package com.example.msaorderservice.order.log;

import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.example.msaorderservice.cart.entity.CartEntity;
import com.example.msaorderservice.order.entity.OrderEntity;

@Aspect
@Component
public class OrderMdcAspect {

	@AfterReturning(
		pointcut =
			"execution(* com.example.msaorderservice.order.repository.OrderRepository.find*(..)) || " +
				"execution(* com.example.msaorderservice.order.repository.OrderRepository.get*(..)) || " +
				"execution(* com.example.msaorderservice.order.repository.OrderRepository.save(..))",
		returning = "ret")
	public void putOrderIdAfterRepo(JoinPoint jp, Object ret
	) {
		UUID orderId = extractOrderId(ret);
		if (orderId != null && MDC.get("orderId") == null) {
			MDC.put("orderId", orderId.toString());
		}
	}

	private UUID extractOrderId(Object ret) {
		if (ret instanceof OrderEntity orderEntity) {
			return orderEntity.getOrderId();
		}
		if (ret instanceof Optional<?> opt && opt.isPresent() && opt.get() instanceof OrderEntity orderEntity) {
			return orderEntity.getOrderId();
		}
		return null;
	}
}
