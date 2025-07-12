package ru.mdemidkin.transfer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.transfer.TransferRequest;
import ru.mdemidkin.transfer.service.TransferService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(TransferController.class)
@WithMockUser
class TransferControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TransferService transferService;

    @BeforeEach
    void init() {
        this.webTestClient = webTestClient.mutateWith(csrf());
    }

    @Test
    void editCashBalance_success() {
        ResponseEntity<Void> resp = ResponseEntity.ok().build();
        when(transferService.processTransfer(eq("user1"), any(TransferRequest.class)))
                .thenReturn(Mono.just(resp));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("from_currency", "USD");
        form.add("to_currency",   "EUR");
        form.add("value",         "100.50");
        form.add("to_login",      "otheruser");

        webTestClient.post()
                .uri("/user/user1/transfer")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .exchange()
                .expectStatus().isOk();

        var captor = ArgumentCaptor.forClass(TransferRequest.class);
        verify(transferService).processTransfer(eq("user1"), captor.capture());
        TransferRequest tr = captor.getValue();
        assertEquals("USD",     tr.getFromCurrency());
        assertEquals("EUR",     tr.getToCurrency());
        assertEquals(new BigDecimal("100.50"), tr.getValue());
        assertEquals("otheruser", tr.getToLogin());
    }

    @Test
    void editCashBalance_serviceError() {
        when(transferService.processTransfer(eq("user2"), any(TransferRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("failed")));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("from_currency", "GBP");
        form.add("to_currency",   "RUB");
        form.add("value",         "42");
        form.add("to_login",      "someone");

        webTestClient.post()
                .uri("/user/user2/transfer")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(transferService).processTransfer(eq("user2"), any(TransferRequest.class));
    }
}
