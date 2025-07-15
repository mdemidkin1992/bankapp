package ru.mdemidkin.transfer.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.account.AccountDto;
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
    @CircuitBreaker(name = "gateway-service", fallbackMethod = "processCashFallback")
    public Mono<CashProcessResponse> processCash(String login, CashRequest cashRequest) {
        return webClient.post()
                .uri("http://" + gateway + "/api/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(cashRequest)
                .retrieve()
                .bodyToMono(CashProcessResponse.class);
    }

    @Retry(name = "gateway-service")
    @CircuitBreaker(name = "gateway-service", fallbackMethod = "getAccountFallback")
    public Mono<AccountDto> getAccount(String login, String currency) {
        return webClient.get()
                .uri("http://" + gateway + "/api/{login}/account/{currency}", login, currency)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToMono(AccountDto.class);
    }

    private Mono<CashProcessResponse> processCashFallback() {
        return Mono.just(CashProcessResponse.builder()
                .status("error")
                .errors(List.of("Ошибка соединения с сервером"))
                .build());
    }

    private Mono<AccountDto> getAccountFallback() {
        return Mono.just(AccountDto.builder()
                .exists(false)
                .build());
    }
}
