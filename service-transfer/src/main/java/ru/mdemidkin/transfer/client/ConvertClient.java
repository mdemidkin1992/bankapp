package ru.mdemidkin.transfer.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.transfer.TransferRequest;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ConvertClient {

    private final WebClient webClient;

    @Value("${services.service-gateway.name}")
    private String gateway;

    @Retry(name = "gateway-service")
    @CircuitBreaker(name = "gateway-service", fallbackMethod = "convertAmountFallback")
    public Mono<BigDecimal> convertAmount(TransferRequest transferRequest) {
        return webClient.post()
                .uri("http://" + gateway + "/api/convert")
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(transferRequest)
                .retrieve()
                .bodyToMono(BigDecimal.class);
    }

    private Mono<BigDecimal> convertAmountFallback() {
        return Mono.just(BigDecimal.ZERO);
    }
}
