package ru.mdemidkin.front.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.account.AccountDto;
import ru.mdemidkin.libdto.account.Currency;
import ru.mdemidkin.libdto.account.UserDto;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountsClient {

    private final WebClient webClient;

    @Value("${services.service-gateway.name}")
    private String gateway;

    @Retry(name = "gateway-service")
    @CircuitBreaker(name = "gateway-service", fallbackMethod = "getUserDtoFallback")
    public Mono<UserDto> getUserDto(String login) {
        return sendRequest("/api/{login}/user", UserDto.class, login);
    }

    @Retry(name = "gateway-service")
    @CircuitBreaker(name = "gateway-service", fallbackMethod = "getAccountsListFallback")
    public Mono<List<AccountDto>> getAccountsList(String login) {
        return sendRequest("/api/{login}/accounts", new ParameterizedTypeReference<>() {
        }, login);
    }

    @Retry(name = "gateway-service")
    @CircuitBreaker(name = "gateway-service", fallbackMethod = "getUsersFallback")
    public Mono<List<UserDto>> getUsers() {
        return sendRequest("/api/users", new ParameterizedTypeReference<>() {
        });
    }

    @Retry(name = "gateway-service")
    @CircuitBreaker(name = "gateway-service", fallbackMethod = "getCurrenciesFallback")
    public Mono<List<Currency>> getCurrencies() {
        return sendRequest("/api/currencies", new ParameterizedTypeReference<>() {
        });
    }

    private Mono<UserDto> getUserDtoFallback(String login) {
        return Mono.just(UserDto.builder()
                .login(login)
                .name("Временно недоступен")
                .build());
    }

    private <T> Mono<T> sendRequest(String uri, Class<T> responseType, Object... uriVariables) {
        return webClient.get()
                .uri("http://" + gateway + uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
    }

    private <T> Mono<T> sendRequest(String uri, ParameterizedTypeReference<T> responseType, Object... uriVariables) {
        return webClient.get()
                .uri("http://" + gateway + uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
    }

    private Mono<List<AccountDto>> getAccountsListFallback(String login) {
        return Mono.just(Collections.emptyList());
    }

    private Mono<List<UserDto>> getUsersFallback(Exception ex) {
        return Mono.just(Collections.emptyList());
    }

    private Mono<List<Currency>> getCurrenciesFallback() {
        return Mono.just(List.of(Currency.RUB));
    }

}
