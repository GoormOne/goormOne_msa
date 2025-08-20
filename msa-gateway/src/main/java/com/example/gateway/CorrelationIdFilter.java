package com.example.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
class CorrelationIdFilter implements GlobalFilter, Ordered {
    private static final String HEADER = "X-Correlation-Id";

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var req = exchange.getRequest();
        var cid = Optional.ofNullable(req.getHeaders().getFirst(HEADER))
                .orElse(UUID.randomUUID().toString());
        var mut = req.mutate().headers(h -> h.set(HEADER, cid)).build();
        return chain.filter(exchange.mutate().request(mut).build())
                .then(Mono.fromRunnable(() ->
                        exchange.getResponse().getHeaders().set(HEADER, cid)));
    }
    public int getOrder() { return -1; } // 먼저 실행
}
