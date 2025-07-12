package ru.mdemidkin.exchange.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.mdemidkin.exchange.model.Currency;
import ru.mdemidkin.exchange.service.ExchangeRateService;
import ru.mdemidkin.libdto.account.CurrencyDto;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@WebFluxTest(ExchangeRatesController.class)
@WithMockUser
class ExchangeRatesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Test
    void getCurrencies_success() {
        Currency c1 = new Currency(1L, "USD", "USD", new BigDecimal("1.0"));
        Currency c2 = new Currency(2L, "EUR", "EUR", new BigDecimal("0.9"));
        when(exchangeRateService.getAllCurrentRates()).thenReturn(Flux.just(c1, c2));

        webTestClient.get()
                .uri("/api/rates")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CurrencyDto.class)
                .hasSize(2)
                .value(list -> {
                    CurrencyDto dto1 = list.get(0);
                    CurrencyDto dto2 = list.get(1);
                    assertEquals("USD", dto1.getName());
                    assertEquals("USD", dto1.getTitle());
                    assertEquals(0, dto1.getValue().compareTo(new BigDecimal("1.0")));
                    assertEquals("EUR", dto2.getName());
                    assertEquals("EUR", dto2.getTitle());
                    assertEquals(0, dto2.getValue().compareTo(new BigDecimal("0.9")));
                });
    }

    @Test
    void getCurrencies_serviceError() {
        when(exchangeRateService.getAllCurrentRates())
                .thenReturn(Flux.error(new RuntimeException("Service failure")));

        webTestClient.get()
                .uri("/api/rates")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
