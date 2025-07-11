package ru.mdemidkin.transfer.client;

import lombok.RequiredArgsConstructor;
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
    private static final String CONVERT_BASE_URL = "http://service-convert";

    public Mono<BigDecimal> convertAmount(TransferRequest transferRequest) {
        return webClient.post()
                .uri(CONVERT_BASE_URL + "/api/convert")
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(transferRequest)
                .retrieve()
                .bodyToMono(BigDecimal.class);
    }
}
