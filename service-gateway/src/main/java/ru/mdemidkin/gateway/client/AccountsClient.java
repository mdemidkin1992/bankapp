package ru.mdemidkin.gateway.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.signup.SignupRequest;
import ru.mdemidkin.libdto.signup.SignupResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountsClient {

    private final WebClient webClient;

    @Value("${services.service-gateway.name}")
    private String gateway;

    @Retry(name = "gateway-service")
    @CircuitBreaker(name = "gateway-service", fallbackMethod = "signupFallback")
    public Mono<SignupResponse> signup(SignupRequest signupRequest) {
        return webClient.post()
                .uri("http://" + gateway + "/api/signup")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(signupRequest)
                .retrieve()
                .bodyToMono(SignupResponse.class);
    }

    private Mono<SignupResponse> signupFallback() {
        return Mono.just(SignupResponse.builder()
                .errors(List.of("Ошибка соединения с сервером"))
                .build());
    }
}