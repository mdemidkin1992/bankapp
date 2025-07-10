package ru.mdemidkin.transfer.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.account.AccountDto;
import ru.mdemidkin.libdto.cash.CashProcessResponse;
import ru.mdemidkin.libdto.transfer.TransferRequest;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ConvertClient {
    private final WebClient webClient;
    private static final String ACCOUNTS_BASE_URL = "http://service-convert";

    public Mono<BigDecimal> convertAmount(String fromCurrency, String toCurrency, BigDecimal bigDecimal) {
        return Mono.just(BigDecimal.TWO);
    }
}
