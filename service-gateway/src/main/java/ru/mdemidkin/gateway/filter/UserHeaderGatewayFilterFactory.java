package ru.mdemidkin.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class UserHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @Value("${headers.user-header}")
    private String userHeader;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> exchange.getPrincipal()
                .cast(Authentication.class)
                .map(Principal::getName)
                .defaultIfEmpty("anonymous")
                .flatMap(username -> {
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .header(userHeader, username)
                            .build();
                    return chain.filter(exchange.mutate().request(request).build());
                });
    }
}

