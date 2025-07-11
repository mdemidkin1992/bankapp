package ru.mdemidkin.convert.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.mdemidkin.libdto.account.CurrencyDto;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ExchangeClient {

    private final WebClient webClient;

    @Value("${services.service-gateway.name}")
    private String gateway;

    public Flux<CurrencyDto> getCurrencies() {
        return webClient.get()
                .uri("http://" + gateway + "/api/rates")
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(CurrencyDto.class);
    }
}
