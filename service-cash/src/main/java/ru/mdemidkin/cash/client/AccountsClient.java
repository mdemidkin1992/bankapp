package ru.mdemidkin.cash.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.cash.dto.CashRequest;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AccountsClient {
    private final WebClient webClient;
    private static final String ACCOUNTS_BASE_URL = "http://service-accounts";

    public Mono<ResponseEntity<String>> sendCashUpdateRequest(String login, CashRequest cashRequest) {
        return webClient.post()
                .uri(ACCOUNTS_BASE_URL + "/api/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(cashRequest)
                .retrieve()
                .toEntity(String.class);
    }

    public Mono<ResponseEntity<String>> sendToMain() {
        return webClient.get()
                .uri(ACCOUNTS_BASE_URL)
                .retrieve()
                .toEntity(String.class);
    }
}
