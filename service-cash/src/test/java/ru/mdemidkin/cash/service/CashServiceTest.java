package ru.mdemidkin.cash.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import ru.mdemidkin.cash.client.AccountsClient;
import ru.mdemidkin.cash.client.BlockersClient;
import ru.mdemidkin.cash.client.NotificationsClient;
import ru.mdemidkin.libdto.cash.CashProcessResponse;
import ru.mdemidkin.libdto.cash.CashRequest;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private BlockersClient blockersClient;

    @Mock
    private NotificationsClient notificationsClient;

    @InjectMocks
    private CashService cashService;

    private final CashRequest request = CashRequest.builder()
            .currency("USD")
            .value("100.00")
            .action(ru.mdemidkin.libdto.cash.CashAction.PUT)
            .build();

    @BeforeEach
    void setUp() {
        when(notificationsClient.sendNotification(anyString(), anyString()))
                .thenReturn(Mono.empty());
    }

    @Test
    void updateCashBalance_notBlocked_completed() {
        when(blockersClient.sendBlockerRequest(anyString()))
                .thenReturn(Mono.just(false));
        CashProcessResponse responseBody = CashProcessResponse.builder()
                .status("completed")
                .errors(null)
                .build();
        when(accountsClient.sendCashUpdateRequest(eq("user1"), eq(request)))
                .thenReturn(Mono.just(ResponseEntity.ok(responseBody)));

        ResponseEntity<Void> result = cashService.updateCashBalance("user1", request).block();

        assertNotNull(result);
        assertEquals(302, result.getStatusCodeValue());
        URI location = result.getHeaders().getLocation();
        assertNotNull(location);
        assertEquals("/", location.getPath());
        assertNull(location.getQuery(), "No errors expected on successful update");

        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        then(notificationsClient).should().sendNotification(eq("user1"), msgCaptor.capture());
        String sentMsg = msgCaptor.getValue();
        assertTrue(sentMsg.contains("Успешное пополнение счета:"));
        assertTrue(sentMsg.contains("USD"));
        assertTrue(sentMsg.contains("100.00"));
    }

    @Test
    void updateCashBalance_notBlocked_failed() {
        // given
        when(blockersClient.sendBlockerRequest(anyString()))
                .thenReturn(Mono.just(false));
        List<String> errors = List.of("err1", "err2");
        CashProcessResponse responseBody = CashProcessResponse.builder()
                .status("failed")
                .errors(errors)
                .build();
        when(accountsClient.sendCashUpdateRequest(eq("user1"), eq(request)))
                .thenReturn(Mono.just(ResponseEntity.ok(responseBody)));

        ResponseEntity<Void> result = cashService.updateCashBalance("user1", request).block();

        assertNotNull(result);
        assertEquals(302, result.getStatusCodeValue());
        URI loc = result.getHeaders().getLocation();
        assertNotNull(loc);
        assertEquals("/", loc.getPath());
        assertEquals("cashErrors=err1&cashErrors=err2", loc.getQuery());

        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        then(notificationsClient).should().sendNotification(eq("user1"), msgCaptor.capture());
        String sentMsg = msgCaptor.getValue();
        assertTrue(sentMsg.contains("Ошибка пополнения счета:"));
    }

    @Test
    void updateCashBalance_blocked() {
        when(blockersClient.sendBlockerRequest(anyString()))
                .thenReturn(Mono.just(true));

        ResponseEntity<Void> result = cashService.updateCashBalance("user1", request).block();

        assertNotNull(result);
        assertEquals(302, result.getStatusCodeValue());
        URI loc = result.getHeaders().getLocation();
        assertNotNull(loc);
        assertEquals("/", loc.getPath());
        assertTrue(loc.getQuery().contains("Операция по пополнению счета заблокирована:"));

        then(notificationsClient).should().sendNotification(eq("user1"), contains("Операция по пополнению счета заблокирована:"));
    }
}
