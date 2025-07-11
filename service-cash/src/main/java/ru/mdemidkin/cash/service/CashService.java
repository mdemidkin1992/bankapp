package ru.mdemidkin.cash.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.mdemidkin.cash.client.AccountsClient;
import ru.mdemidkin.cash.client.BlockersClient;
import ru.mdemidkin.cash.client.NotificationsClient;
import ru.mdemidkin.libdto.cash.CashProcessResponse;
import ru.mdemidkin.libdto.cash.CashRequest;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService {

    private final AccountsClient accountsClient;
    private final BlockersClient blockersClient;
    private final NotificationsClient notificationsClient;

    private static final String SUCCESS_MESSAGE = "Успешное пополнение счета:";
    private static final String FAIL_MESSAGE = "Ошибка пополнения счета:";
    private static final String BLOCKED_MESSAGE = "Операция по пополнению счета заблокирована:";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Mono<ResponseEntity<Void>> updateCashBalance(String login, CashRequest cashRequest) {
        String dateTime = LocalDateTime.now().format(FORMATTER);
        return blockersClient.sendBlockerRequest(dateTime)
                .flatMap(isBlocked -> {
                    if (!isBlocked) {
                        return accountsClient.sendCashUpdateRequest(login, cashRequest)
                                .doOnNext(response -> {
                                    CashProcessResponse body = response.getBody();
                                    log.info("тело ответа {}", body);
                                    if (body != null && "completed".equals(body.getStatus())) {
                                        notificationsClient.sendNotification(login, formatMessage(SUCCESS_MESSAGE, cashRequest)).subscribe();
                                    } else {
                                        notificationsClient.sendNotification(login, formatMessage(FAIL_MESSAGE, cashRequest)).subscribe();
                                    }
                                })
                                .flatMap(response -> redirectToMain(response.getBody().getErrors()));
                    } else {
                        notificationsClient.sendNotification(login, formatMessage(BLOCKED_MESSAGE, cashRequest)).subscribe();
                        return redirectToMain(List.of(BLOCKED_MESSAGE));
                    }
                });
    }

    private String formatMessage(String message, CashRequest cashRequest) {
        String dateTime = LocalDateTime.now().format(FORMATTER);
        return String.format(dateTime + " " + message + " %s %s", cashRequest.getCurrency(), cashRequest.getValue());
    }

    private Mono<ResponseEntity<Void>> redirectToMain(@Nullable List<String> errors) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/");

        if (errors != null) {
            builder.queryParam("cashErrors", errors);
        }

        URI location = builder
                .build()
                .toUri();

        return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .build());
    }
}
