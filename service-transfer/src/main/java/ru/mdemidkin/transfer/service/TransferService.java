package ru.mdemidkin.transfer.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.account.AccountDto;
import ru.mdemidkin.libdto.cash.CashAction;
import ru.mdemidkin.libdto.cash.CashRequest;
import ru.mdemidkin.transfer.client.AccountsClient;
import ru.mdemidkin.transfer.client.BlockersClient;
import ru.mdemidkin.transfer.client.ConvertClient;
import ru.mdemidkin.transfer.client.NotificationsClient;
import ru.mdemidkin.libdto.transfer.TransferRequest;
import ru.mdemidkin.transfer.exception.AccountNotFoundException;
import ru.mdemidkin.transfer.exception.InsufficientFundsException;
import ru.mdemidkin.transfer.exception.TransferException;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountsClient accountsClient;
    private final BlockersClient blockersClient;
    private final NotificationsClient notificationsClient;
    private final ConvertClient convertClient;

    private static final String SUCCESS_MESSAGE = "Успешный перевод:";
    private static final String FAIL_MESSAGE = "Ошибка перевода:";
    private static final String BLOCKED_MESSAGE = "Операция по переводу заблокирована:";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Mono<ResponseEntity<Void>> processTransfer(String login, TransferRequest transferRequest) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        List<String> transferErrors = new ArrayList<>();
        List<String> transferOtherErrors = new ArrayList<>();

        return blockersClient.sendBlockerRequest(timestamp)
                .flatMap(blocked -> {
                    if (blocked) {
                        notificationsClient.sendNotification(login, formatMessage(BLOCKED_MESSAGE, transferRequest)).subscribe();
                        addErrors(BLOCKED_MESSAGE, login, transferRequest.getToLogin(), transferErrors, transferOtherErrors);
                        return redirectToMain(transferErrors, transferOtherErrors);
                    }

                    return Mono.zip(
                                    accountsClient.getAccount(login, transferRequest.getFromCurrency()),
                                    accountsClient.getAccount(transferRequest.getToLogin(), transferRequest.getToCurrency())
                            )
                            .flatMap(accounts -> {
                                AccountDto fromAccount = accounts.getT1();
                                AccountDto toAccount = accounts.getT2();

                                if (!fromAccount.isExists() || !toAccount.isExists()) {
                                    String message = "Один из счетов не найден";
                                    addErrors(message, login, transferRequest.getToLogin(), transferErrors, transferOtherErrors);
                                    return Mono.error(new AccountNotFoundException(message));
                                }

                                if (fromAccount.getValue().compareTo(transferRequest.getValue()) < 0) {
                                    String message = "Недостаточно средств";
                                    addErrors(message, login, transferRequest.getToLogin(), transferErrors, transferOtherErrors);
                                    return Mono.error(new InsufficientFundsException(message));
                                }

                                return convertClient.convertAmount(
                                                transferRequest.getFromCurrency(),
                                                transferRequest.getToCurrency(),
                                                transferRequest.getValue()
                                        )
                                        .flatMap(convertedAmount -> {
                                            CashRequest withdrawRequest = CashRequest.builder()
                                                    .currency(transferRequest.getFromCurrency())
                                                    .value(transferRequest.getValue().toString())
                                                    .action(CashAction.GET)
                                                    .build();

                                            return accountsClient.processCash(login, withdrawRequest)
                                                    .flatMap(withdrawResponse -> {
                                                        if (!"completed".equals(withdrawResponse.getStatus())) {
                                                            String message = "Ошибка списания: " +
                                                                    String.join(", ", withdrawResponse.getErrors());
                                                            addErrors(message, login, transferRequest.getToLogin(), transferErrors, transferOtherErrors);
                                                            return Mono.error(new TransferException(message));
                                                        }

                                                        CashRequest depositRequest = CashRequest.builder()
                                                                .currency(transferRequest.getToCurrency())
                                                                .value(convertedAmount.toString())
                                                                .action(CashAction.PUT)
                                                                .build();

                                                        return accountsClient.processCash(transferRequest.getToLogin(), depositRequest)
                                                                .flatMap(depositResponse -> {
                                                                    if (!"completed".equals(depositResponse.getStatus())) {
                                                                        String message = "Ошибка списания: " +
                                                                                String.join(", ", depositResponse.getErrors());
                                                                        addErrors(message, login, transferRequest.getToLogin(), transferErrors, transferOtherErrors);
                                                                        return Mono.error(new TransferException(message));
                                                                    }
                                                                    return redirectToMain(transferErrors, transferOtherErrors);
                                                                });
                                                    });
                                        })
                                        .doOnSuccess(v -> {
                                            String message = formatMessage(SUCCESS_MESSAGE, transferRequest);
                                            notificationsClient.sendNotification(login, message).subscribe();
                                        });
                            })
                            .onErrorResume(ex -> {
                                String errorMessage = formatMessage("Ошибка перевода: " + ex.getMessage(), transferRequest);
                                notificationsClient.sendNotification(login, errorMessage).subscribe();
                                return redirectToMain(transferErrors, transferOtherErrors);
                            });
                });
    }

    private void addErrors(String message,
                           String fromLogin,
                           String toLogin,
                           List<String> transferErrors,
                           List<String> transferOtherErrors) {
        if (fromLogin.equals(toLogin)) {
            transferErrors.add(message);
        } else {
            transferOtherErrors.add(message);
        }
    }

    private String formatMessage(String message, TransferRequest transferRequest) {
        String dateTime = LocalDateTime.now().format(FORMATTER);
        return String.format(dateTime + " " + message + " пользователю %s на сумму %s %s в %s",
                transferRequest.getToLogin(),
                transferRequest.getFromCurrency(),
                transferRequest.getValue(),
                transferRequest.getToCurrency()
        );
    }

    private Mono<ResponseEntity<Void>> redirectToMain(@Nullable List<String> transferErrors,
                                                      @Nullable List<String> transferOtherErrors) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/");

        if (transferErrors != null) {
            builder.queryParam("transferErrors", transferErrors);
        }
        if (transferOtherErrors != null) {
            builder.queryParam("transferOtherErrors", transferOtherErrors);
        }

        URI location = builder
                .build()
                .toUri();

        return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .build());
    }
}
