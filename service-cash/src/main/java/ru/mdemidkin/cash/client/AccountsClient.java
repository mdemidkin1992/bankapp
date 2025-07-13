package ru.mdemidkin.cash.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.cash.CashProcessResponse;
import ru.mdemidkin.libdto.cash.CashRequest;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountsClient {

    private final WebClient webClient;

    @Value("${services.service-gateway.name}")
    private String gateway;

    @Retry(name = "gateway-service")
    @CircuitBreaker(name = "gateway-service", fallbackMethod = "sendCashUpdateRequestFallback")
    public Mono<ResponseEntity<CashProcessResponse>> sendCashUpdateRequest(String login, CashRequest cashRequest) {
        return webClient.post()
                .uri("http://" + gateway + "/api/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(cashRequest)
                .retrieve()
                .toEntity(CashProcessResponse.class);
    }

    private Mono<ResponseEntity<CashProcessResponse>> sendCashUpdateRequestFallback() {
        CashProcessResponse errorResponse = CashProcessResponse.builder()
                .status("error")
                .errors(List.of("Ошибка соединения с сервером"))
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(errorResponse));
    }
}
