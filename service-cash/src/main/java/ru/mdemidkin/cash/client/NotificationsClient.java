package ru.mdemidkin.cash.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.notification.NotificationDto;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class NotificationsClient {

    private final WebClient webClient;

    @Value("${services.service-gateway.name}")
    private String gateway;

    public Mono<NotificationDto> sendNotification(String login, String message) {
        return webClient.post()
                .uri("http://" + gateway + "/api/{login}/notifications", login)
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(message)
                .retrieve()
                .bodyToMono(NotificationDto.class);
    }
}
