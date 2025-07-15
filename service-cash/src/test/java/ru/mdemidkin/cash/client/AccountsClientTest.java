package ru.mdemidkin.cash.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.cash.CashAction;
import ru.mdemidkin.libdto.cash.CashProcessResponse;
import ru.mdemidkin.libdto.cash.CashRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountsClientTest {

    private WireMockServer wireMock;
    private AccountsClient accountsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    @SneakyThrows
    void setUp() {
        wireMock = new WireMockServer(
                WireMockConfiguration.options()
                        .dynamicPort());
        wireMock.start();

        WebClient webClient = WebClient.builder().build();
        accountsClient = new AccountsClient(webClient);
        ReflectionTestUtils.setField(
                accountsClient,
                "gateway",
                "localhost:" + wireMock.port()
        );

        wireMock.stubFor(post(urlEqualTo("/api/joe/cash"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(getCashRequest())))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(getCashProcessResponse()))
                )
        );
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void sendCashUpdateRequest_shouldReturnCompleted() {
        CashRequest req = CashRequest.builder()
                .currency("EUR")
                .action(CashAction.PUT)
                .value("100.00")
                .build();

        Mono<org.springframework.http.ResponseEntity<CashProcessResponse>> mono =
                accountsClient.sendCashUpdateRequest("joe", req);

        org.springframework.http.ResponseEntity<CashProcessResponse> resp = mono.block();
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCodeValue());

        CashProcessResponse body = resp.getBody();
        assertNotNull(body);
        assertEquals("completed", body.getStatus());
        assertNull(body.getErrors());
    }

    private CashRequest getCashRequest() {
        return CashRequest.builder()
                .currency("EUR")
                .action(CashAction.PUT)
                .value("100.00")
                .build();
    }

    private CashProcessResponse getCashProcessResponse() {
        return CashProcessResponse.builder()
                .status("completed")
                .errors(null)
                .build();
    }
}
