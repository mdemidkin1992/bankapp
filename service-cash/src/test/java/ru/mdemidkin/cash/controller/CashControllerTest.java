package ru.mdemidkin.cash.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import ru.mdemidkin.cash.service.CashService;
import ru.mdemidkin.libdto.cash.CashRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(CashController.class)
@WithMockUser
class CashControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CashService cashService;

    @BeforeEach
    void init() {
        this.webTestClient = webTestClient.mutateWith(csrf());
    }

    @Test
    void editCashBalance_success() {
        // given
        when(cashService.updateCashBalance(eq("user1"), any(CashRequest.class)))
                .thenReturn(Mono.just(ResponseEntity.ok().<Void>build()));

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("currency", "USD");
        formData.add("action", "PUT");
        formData.add("value", "100.00");

        // when / then
        webTestClient.post()
                .uri("/user/user1/cash")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    void editCashBalance_serviceError() {
        // given
        when(cashService.updateCashBalance(eq("user1"), any(CashRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("failed")));

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("currency", "EUR");
        formData.add("action", "TAKE");
        formData.add("value", "50.00");

        // when / then
        webTestClient.post()
                .uri("/user/user1/cash")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
