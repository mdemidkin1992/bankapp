package ru.mdemidkin.convert.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.convert.service.ConvertService;
import ru.mdemidkin.libdto.transfer.TransferRequest;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(ConvertController.class)
@WithMockUser
class ConvertControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ConvertService convertService;

    @BeforeEach
    void init() {
        this.webTestClient = webTestClient.mutateWith(csrf());
    }

    @Test
    void convertAmount_success() {
        TransferRequest request = TransferRequest.builder()
                .fromCurrency("USD")
                .toCurrency("EUR")
                .value(new BigDecimal("100.00"))
                .build();
        BigDecimal expected = new BigDecimal("85.50");
        when(convertService.convertAmount(eq("USD"), eq("EUR"), eq(new BigDecimal("100.00"))))
                .thenReturn(Mono.just(expected));

        webTestClient.post()
                .uri("/api/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class).isEqualTo(expected);
    }

    @Test
    void convertAmount_serviceError() {
        TransferRequest request = TransferRequest.builder()
                .fromCurrency("GBP")
                .toCurrency("JPY")
                .value(new BigDecimal("50.00"))
                .build();
        when(convertService.convertAmount(any(String.class), any(String.class), any(BigDecimal.class)))
                .thenReturn(Mono.error(new RuntimeException("Conversion failed")));

        webTestClient.post()
                .uri("/api/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
