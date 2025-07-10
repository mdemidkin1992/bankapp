package ru.mdemidkin.transfer.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.account.AccountDto;
import ru.mdemidkin.libdto.cash.CashRequest;
import ru.mdemidkin.libdto.transfer.TransferRequest;
import ru.mdemidkin.libdto.cash.CashProcessResponse;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AccountsClient {
    private final WebClient webClient;
    private static final String ACCOUNTS_BASE_URL = "http://service-accounts";

    public Mono<CashProcessResponse> processCash(String login, CashRequest cashRequest) {
        return webClient.post()
                .uri(ACCOUNTS_BASE_URL + "/api/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(cashRequest)
                .retrieve()
                .bodyToMono(CashProcessResponse.class);
    }

    public Mono<AccountDto> getAccount(String login, String currency) {
        return webClient.get()
                .uri(ACCOUNTS_BASE_URL + "/api/{login}/account/{currency}", login, currency)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToMono(AccountDto.class);
    }
}
