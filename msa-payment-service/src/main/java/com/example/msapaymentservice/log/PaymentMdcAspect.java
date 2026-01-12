package com.example.msapaymentservice.log;

import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.example.msapaymentservice.entity.PaymentEntity;

@Aspect
@Component
public class PaymentMdcAspect {

	@AfterReturning(
		pointcut =
			"execution(* com.example.msapaymentservice.repository.PaymentRepository.find*(..)) || " +
				"execution(* com.example.msapaymentservice.repository.PaymentRepository.get*(..)) || " +
				"execution(* com.example.msapaymentservice.repository.PaymentRepository.save(..))",
		returning = "ret")
	public void putPaymentIdAfterRepo(JoinPoint jp, Object ret) {
		UUID paymentId = extractPaymentId(ret);
		if (paymentId != null && MDC.get("paymentId") == null) {
			MDC.put("paymentId", paymentId.toString());
		}
	}

	private UUID extractPaymentId(Object ret) {
		if (ret instanceof PaymentEntity paymentEntity) {
			return paymentEntity.getPaymentId();
		}
		if (ret instanceof Optional<?> opt && opt.isPresent() && opt.get() instanceof PaymentEntity paymentEntity) {
			return paymentEntity.getPaymentId();
		}
		return null;
	}
}
