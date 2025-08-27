package com.example.storeservice.aop;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.example.storeservice.global.exception.InventoryProcessingException;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

	private final RedissonClient redissonClient;
	private final ExpressionParser parser = new SpelExpressionParser();
	private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

	@Around("@annotation(lock)")
	public Object around(ProceedingJoinPoint joinPoint, DistributedLock lock) throws Throwable {
		String key = buildKey(joinPoint, lock.key());
		RLock rLock = redissonClient.getLock(key);

		final int maxRetires = 5;
		final long baseBackOffMs = 200L;

		boolean acquired = false;
		int attempts = 0;

		while (attempts < maxRetires && !acquired) {
			attempts++;
			acquired = rLock.tryLock(lock.waitTime(), lock.leaseTime(), lock.timeUnit());
			if (!acquired && attempts < maxRetires) {
				long jitter = ThreadLocalRandom.current().nextLong(0, 30);
				long sleep = baseBackOffMs * attempts + jitter;
				try {
					Thread.sleep(sleep);
				} catch (InventoryProcessingException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}

		if (!acquired) {
			throw new InventoryProcessingException("분산락 획득 실패: " + key);
		}

		try {
			return joinPoint.proceed();
		} finally{
			if (acquired && rLock.isHeldByCurrentThread()) {
				rLock.unlock();
			}
		}
	}

	private String buildKey(ProceedingJoinPoint joinPoint, String keyTemplate) {
		if (keyTemplate == null || keyTemplate.isBlank()) {
			throw new IllegalArgumentException("DistributedLock.key must not be blank");
		}

		if (!keyTemplate.contains("#{")) {
			return "lock:" + keyTemplate;
		}

		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		Method method = signature.getMethod();
		String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
		Object[] args = joinPoint.getArgs();

		EvaluationContext context = new StandardEvaluationContext();
		if (paramNames != null) {
			for (int i = 0; i < paramNames.length; i++) {
				context.setVariable(paramNames[i], args[i]);
			}
		}
		String evaluated = parser.parseExpression(keyTemplate, new TemplateParserContext())
			.getValue(context, String.class);
		return "lock:" + evaluated;
	}

}
