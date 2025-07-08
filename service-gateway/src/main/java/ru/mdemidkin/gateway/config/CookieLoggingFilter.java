package ru.mdemidkin.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(-100)
//fixme удалить после отладки
public class CookieLoggingFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(CookieLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        log.info("=== Request: {} {}", request.getMethod(), request.getPath());
        request.getCookies().forEach((name, cookies) -> {
            cookies.forEach(cookie -> {
                log.info("Cookie: {} = {}", name, cookie.getValue());
            });
        });

        return chain.filter(exchange);
    }
}