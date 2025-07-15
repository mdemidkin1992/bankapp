package ru.mdemidkin.convert.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.mdemidkin.libdto.account.CurrencyDto;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeClientTest {

    private WireMockServer wireMock;
    private ExchangeClient exchangeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();

        WebClient webClient = WebClient.builder().build();
        exchangeClient = new ExchangeClient(webClient);
        ReflectionTestUtils.setField(
                exchangeClient,
                "gateway",
                "localhost:" + wireMock.port()
        );

        List<CurrencyDto> stubCurrencies = List.of(
                new CurrencyDto("USD", "Dollar", BigDecimal.valueOf(1.0)),
                new CurrencyDto("EUR", "Euro", BigDecimal.valueOf(0.9))
        );
        String responseJson = objectMapper.writeValueAsString(stubCurrencies);

        wireMock.stubFor(get(urlPathEqualTo("/api/rates"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(responseJson)
                ));
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void getCurrencies_whenSuccessful_returnsList() {
        Flux<CurrencyDto> flux = exchangeClient.getCurrencies();
        List<CurrencyDto> currencies = flux.collectList().block();

        assertNotNull(currencies);
        assertEquals(2, currencies.size());

        CurrencyDto usd = currencies.get(0);
        assertEquals("USD", usd.getTitle());
        assertEquals("Dollar", usd.getName());
        assertEquals(0, usd.getValue().compareTo(BigDecimal.valueOf(1.0)));

        CurrencyDto eur = currencies.get(1);
        assertEquals("EUR", eur.getTitle());
        assertEquals("Euro", eur.getName());
        assertEquals(0, eur.getValue().compareTo(BigDecimal.valueOf(0.9)));
    }

    @Test
    void getCurrencies_whenServerError_throwsException() {
        wireMock.stubFor(get(urlPathEqualTo("/api/rates"))
                .willReturn(aResponse().withStatus(500))
        );

        assertThrows(Exception.class,
                () -> exchangeClient.getCurrencies().collectList().block()
        );
    }
}
