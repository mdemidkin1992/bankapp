package ru.mdemidkin.transfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.account.AccountDto;
import ru.mdemidkin.libdto.cash.CashProcessResponse;
import ru.mdemidkin.libdto.cash.CashRequest;
import ru.mdemidkin.libdto.transfer.TransferRequest;
import ru.mdemidkin.transfer.client.AccountsClient;
import ru.mdemidkin.transfer.client.BlockersClient;
import ru.mdemidkin.transfer.client.ConvertClient;
import ru.mdemidkin.transfer.client.NotificationsClient;

import java.math.BigDecimal;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private BlockersClient blockersClient;

    @Mock
    private NotificationsClient notificationsClient;

    @Mock
    private ConvertClient convertClient;

    @InjectMocks
    private TransferService transferService;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private TransferRequest request;
    private String login;

    @BeforeEach
    void init() {
        login = "alice";
        request = TransferRequest.builder()
                .fromCurrency("USD")
                .toCurrency("EUR")
                .value(new BigDecimal("100"))
                .toLogin("bob")
                .build();
        when(notificationsClient.sendNotification(anyString(), anyString()))
                .thenReturn(Mono.empty());
    }

    @Test
    void processTransfer_whenBlocked_shouldRedirectWithBlockedError() {
        when(blockersClient.sendBlockerRequest(anyString()))
                .thenReturn(Mono.just(true));

        ResponseEntity<Void> resp = transferService
                .processTransfer(login, request)
                .block();

        assertNotNull(resp);
        assertEquals(HttpStatus.FOUND, resp.getStatusCode());
        URI loc = resp.getHeaders().getLocation();
        assertNotNull(loc);
        assertTrue(loc.toString().contains("transferOtherErrors"));
        verify(notificationsClient).sendNotification(eq(login), messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("Операция по переводу заблокирована:"));
    }

    @Test
    void processTransfer_whenAccountNotFound_shouldRedirectWithAccountError() {
        when(blockersClient.sendBlockerRequest(anyString()))
                .thenReturn(Mono.just(false));
        AccountDto missing = AccountDto.builder().exists(false).build();
        AccountDto present = AccountDto.builder().exists(true).build();
        when(accountsClient.getAccount(eq(login), eq("USD")))
                .thenReturn(Mono.just(missing));
        when(accountsClient.getAccount(eq("bob"), eq("EUR")))
                .thenReturn(Mono.just(present));

        ResponseEntity<Void> resp = transferService
                .processTransfer(login, request)
                .block();

        assertNotNull(resp);
        assertEquals(HttpStatus.FOUND, resp.getStatusCode());
        URI loc = resp.getHeaders().getLocation();
        assertNotNull(loc);
        assertTrue(loc.toString().contains("transferOtherErrors"));
        verify(notificationsClient).sendNotification(eq(login), messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("Ошибка перевода: Один из счетов не найден"));
    }

    @Test
    void processTransfer_whenInsufficientFunds_shouldRedirectWithFundsError() {
        when(blockersClient.sendBlockerRequest(anyString()))
                .thenReturn(Mono.just(false));
        AccountDto from = AccountDto.builder()
                .exists(true)
                .value(new BigDecimal("50"))
                .build();
        AccountDto to = AccountDto.builder()
                .exists(true)
                .value(new BigDecimal("0"))
                .build();
        when(accountsClient.getAccount(eq(login), eq("USD")))
                .thenReturn(Mono.just(from));
        when(accountsClient.getAccount(eq("bob"), eq("EUR")))
                .thenReturn(Mono.just(to));

        ResponseEntity<Void> resp = transferService
                .processTransfer(login, request)
                .block();

        assertNotNull(resp);
        assertEquals(HttpStatus.FOUND, resp.getStatusCode());
        URI loc = resp.getHeaders().getLocation();
        assertNotNull(loc);
        assertTrue(loc.toString().contains("transferErrors"));
        verify(notificationsClient).sendNotification(eq(login), messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("Ошибка перевода: Недостаточно средств"));
    }

    @Test
    void processTransfer_successfulFlow_shouldRedirectToRootAndNotifySuccess() {
        when(blockersClient.sendBlockerRequest(anyString()))
                .thenReturn(Mono.just(false));
        AccountDto from = AccountDto.builder()
                .exists(true).value(new BigDecimal("100")).build();
        AccountDto to = AccountDto.builder()
                .exists(true).value(new BigDecimal("0")).build();
        when(accountsClient.getAccount(eq(login), eq("USD")))
                .thenReturn(Mono.just(from));
        when(accountsClient.getAccount(eq("bob"), eq("EUR")))
                .thenReturn(Mono.just(to));
        when(convertClient.convertAmount(eq(request)))
                .thenReturn(Mono.just(new BigDecimal("90")));
        CashProcessResponse withdrawResp = new CashProcessResponse();
        withdrawResp.setStatus("completed");
        withdrawResp.setErrors(null);
        when(accountsClient.processCash(eq(login), any(CashRequest.class)))
                .thenReturn(Mono.just(withdrawResp));
        CashProcessResponse depositResp = new CashProcessResponse();
        depositResp.setStatus("completed");
        depositResp.setErrors(null);
        when(accountsClient.processCash(eq("bob"), any(CashRequest.class)))
                .thenReturn(Mono.just(depositResp));

        ResponseEntity<Void> resp = transferService
                .processTransfer(login, request)
                .block();

        assertNotNull(resp);
        assertEquals(HttpStatus.FOUND, resp.getStatusCode());
        verify(notificationsClient).sendNotification(eq(login), messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("Успешный перевод:"));
    }
}
