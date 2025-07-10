package ru.mdemidkin.cash.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class BlockersClient {
    private final WebClient webClient;
    private static final String BLOCKER_BASE_URL = "http://service-blocker";

    public Mono<Boolean> sendBlockerRequest(String time) {
        return webClient.post()
                .uri(BLOCKER_BASE_URL + "/api/{time}/block", time)
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToMono(Boolean.class);
    }
}
