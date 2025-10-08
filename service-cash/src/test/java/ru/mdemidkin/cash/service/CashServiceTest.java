package ru.mdemidkin.cash.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import ru.mdemidkin.cash.client.AccountsClient;
import ru.mdemidkin.cash.client.BlockersClient;
import ru.mdemidkin.cash.producer.NotificationsProducerService;
import ru.mdemidkin.libdto.cash.CashProcessResponse;
import ru.mdemidkin.libdto.cash.CashRequest;
import ru.mdemidkin.libdto.notification.NotificationDto;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private BlockersClient blockersClient;

    @Mock
    private NotificationsProducerService notificationsProducer;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private CashService cashService;

    private final CashRequest request = CashRequest.builder()
            .currency("USD")
            .value("100.00")
            .action(ru.mdemidkin.libdto.cash.CashAction.PUT)
            .build();

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

        then(notificationsProducer).should().sendNotificationsMessage(eq("user1"), any(NotificationDto.class));
    }

    @Test
    void updateCashBalance_notBlocked_failed() {
        // given
        Counter mockCounter = mock(Counter.class);

        when(meterRegistry.counter(
                eq("cash_failed_by_login"),
                eq("login"),
                anyString())
        ).thenReturn(mockCounter);

        when(meterRegistry.counter(
                eq("cash_failed_by_account"),
                eq("currency"),
                anyString())
        ).thenReturn(mockCounter);

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

        then(notificationsProducer).should().sendNotificationsMessage(eq("user1"), any(NotificationDto.class));
    }

    @Test
    void updateCashBalance_blocked() {
        Counter mockCounter = mock(Counter.class);

        when(meterRegistry.counter(
                eq("cash_blocked_by_login"),
                eq("login"),
                anyString())
        ).thenReturn(mockCounter);

        when(meterRegistry.counter(
                eq("cash_blocked_by_account"),
                eq("currency"),
                anyString())
        ).thenReturn(mockCounter);

        when(blockersClient.sendBlockerRequest(anyString()))
                .thenReturn(Mono.just(true));

        ResponseEntity<Void> result = cashService.updateCashBalance("user1", request).block();

        assertNotNull(result);
        assertEquals(302, result.getStatusCodeValue());
        URI loc = result.getHeaders().getLocation();
        assertNotNull(loc);
        assertEquals("/", loc.getPath());
        assertTrue(loc.getQuery().contains("Операция по пополнению счета заблокирована:"));

        then(notificationsProducer).should().sendNotificationsMessage(eq("user1"), any(NotificationDto.class));
    }
}
