package ru.mdemidkin.transfer.client;

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

@Service
@RequiredArgsConstructor
public class AccountsClient {

    private final WebClient webClient;

    @Value("${services.service-gateway.name}")
    private String gateway;

    public Mono<CashProcessResponse> processCash(String login, CashRequest cashRequest) {
        return webClient.post()
                .uri("http://" + gateway + "/api/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(cashRequest)
                .retrieve()
                .bodyToMono(CashProcessResponse.class);
    }

    public Mono<AccountDto> getAccount(String login, String currency) {
        return webClient.get()
                .uri("http://" + gateway + "/api/{login}/account/{currency}", login, currency)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToMono(AccountDto.class);
    }
}
