package ru.mdemidkin.cash.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.NotificationDto;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class NotificationsClient {
    private final WebClient webClient;
    private static final String NOTIFICATIONS_BASE_URL = "http://service-notifications";

    public Mono<NotificationDto> sendNotification(String login, String message) {
        return webClient.post()
                .uri(NOTIFICATIONS_BASE_URL + "/api/{login}/notifications", login)
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(message)
                .retrieve()
                .bodyToMono(NotificationDto.class);
    }
}
