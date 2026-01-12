package com.example.msaorderservice.cart.log;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

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

		MDC.put("correlationId", corr);
		if (user != null && !user.isBlank()) MDC.put("customerId", user);

		String cartId = req.getParameter("cartId");
		if (cartId == null || cartId.isBlank()) {
			var matcher = Pattern.compile("([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})")
				.matcher(req.getRequestURI());
			if (matcher.find()) {
				cartId = matcher.group(1);
			}
		}

		if (cartId != null && !cartId.isBlank()) {
			MDC.put("cartId", cartId);
		}

		try {
			chain.doFilter(req, res);
		} finally {
			MDC.clear();
		}
	}
}
