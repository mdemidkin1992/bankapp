package ru.mdemidkin.cash.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import ru.mdemidkin.cash.client.AccountsClient;
import ru.mdemidkin.cash.dto.CashRequest;
import ru.mdemidkin.cash.model.Notification;
import ru.mdemidkin.libdto.AccountDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CashService {

    private final AccountsClient accountsClient;
    private final BlockerService blockerService;
    private final NotificationService notificationService;

    private static final String SUCCESS_MESSAGE = "Успешная транзакция";
    private static final String FAIL_MESSAGE = "Ошибка транзакции";
    private static final String BLOCKED_MESSAGE = "Операция заблокирована";

    public Mono<ResponseEntity<String>> updateCashBalance(String login, CashRequest cashRequest) {
        return blockerService.isBlocked(LocalDateTime.now())
                .flatMap(isBlocked -> {
                    if (!isBlocked) {
                        return accountsClient.sendCashUpdateRequest(login, cashRequest)
                                .doOnNext(response -> {
                                    if (response.getStatusCode().is2xxSuccessful()) {
                                        notificationService.notify(login, formatMessage(SUCCESS_MESSAGE, cashRequest)).subscribe();
                                    } else {
                                        notificationService.notify(login, formatMessage(FAIL_MESSAGE, cashRequest)).subscribe();
                                    }
                                });
                    } else {
                        notificationService.notify(login, formatMessage(BLOCKED_MESSAGE, cashRequest)).subscribe();
                        return accountsClient.sendCashUpdateRequest(login, cashRequest);
                    }
                });
    }

    private String formatMessage(String message, CashRequest cashRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm");
        String dateTime = LocalDateTime.now().format(formatter);
        return String.format(dateTime + " " + message + "%s %s", cashRequest.getCurrency(), cashRequest.getValue());
    }

}
