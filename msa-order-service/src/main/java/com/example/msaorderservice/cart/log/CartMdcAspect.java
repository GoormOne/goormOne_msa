package com.example.msaorderservice.cart.log;

import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.example.msaorderservice.cart.entity.CartEntity;

@Aspect
@Component
public class CartMdcAspect {

	@AfterReturning(
		pointcut =
			"execution(* com.example.msaorderservice.cart.repository.CartRepository.find*(..)) || " +
				"execution(* com.example.msaorderservice.cart.repository.CartRepository.get*(..)) || " +
				"execution(* com.example.msaorderservice.cart.repository.CartRepository.save(..))",
		returning = "ret")
	public void putCartIdAfterRepo(JoinPoint jp, Object ret
	) {
		UUID cartId = extractCartId(ret);
		if (cartId != null && MDC.get("cartId") == null) {
			MDC.put("cartId", cartId.toString());
		}
	}

	private UUID extractCartId(Object ret) {
		if (ret instanceof CartEntity cartEntity) {
			return cartEntity.getCartId();
		}
		if (ret instanceof Optional<?> opt && opt.isPresent() && opt.get() instanceof CartEntity cartEntity) {
			return cartEntity.getCartId();
		}
		return null;
	}
}
