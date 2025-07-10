package ru.mdemidkin.cash.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.cash.dto.CashRequest;
import ru.mdemidkin.libdto.cash.CashProcessResponse;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AccountsClient {
    private final WebClient webClient;
    private static final String ACCOUNTS_BASE_URL = "http://service-accounts";

    public Mono<ResponseEntity<CashProcessResponse>> sendCashUpdateRequest(String login, CashRequest cashRequest) {
        return webClient.post()
                .uri(ACCOUNTS_BASE_URL + "/api/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(cashRequest)
                .retrieve()
                .toEntity(CashProcessResponse.class);
    }
}
