package com.example.msapaymentservice.log;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MdcRequestFilter extends OncePerRequestFilter {

	public static final String HDR_CORRELATION_ID = "X-Correlation-Id";
	private static final Pattern UUID_PATTERN = Pattern.compile(
		"([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})"
	);

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
	throws ServletException, IOException {

		String corr =req.getHeader(HDR_CORRELATION_ID);
		if (corr == null || corr.isEmpty()) corr = UUID.randomUUID().toString();
		MDC.put(HDR_CORRELATION_ID, corr);

		putIfPresent("orderId", extractUuid(req, "orderId"));
		putIfPresent("paymentId", extractUuid(req, "paymentId"));;

		try {
			chain.doFilter(req, res);
		} finally {
			MDC.clear();
		}
	}
	private void putIfPresent(String key, String value) {
		if (value != null && !value.isBlank()) {
			MDC.put(key, value);
		}
	}

	private String extractUuid(HttpServletRequest req, String param) {
		String val = req.getParameter(param);
		if (val != null && !val.isBlank()) return val;

		var m = UUID_PATTERN.matcher(req.getRequestURI());
		return m.find() ? m.group(1) : null;
	}
}
