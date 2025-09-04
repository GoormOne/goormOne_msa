package com.example.msaorderservice.cart.log;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class MdcRequestFilter extends OncePerRequestFilter {


	public static final String HDR_CORRELATION_ID = "X-Correlation-Id";
	public static final String HDR_USER_ID = "X-User-Id";

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
		throws ServletException, IOException {

		String corr = req.getHeader(HDR_CORRELATION_ID);
		if (corr == null || corr.isBlank()) corr = UUID.randomUUID().toString();

		String user = req.getHeader(HDR_USER_ID);

		// 요청 시작에 기본 컨텍스트 주입
		MDC.put("correlationId", corr);
		if (user != null && !user.isBlank()) MDC.put("customerId", user);

		try {
			chain.doFilter(req, res);
		} finally {
			// 누수 방지: 무조건 비우기
			MDC.clear();
		}
	}
}
