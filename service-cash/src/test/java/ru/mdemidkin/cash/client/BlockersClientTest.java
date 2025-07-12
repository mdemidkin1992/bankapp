package ru.mdemidkin.cash.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockersClientTest {

    private WireMockServer wireMock;
    private BlockersClient blockersClient;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(
                WireMockConfiguration.options()
                        .dynamicPort());
        wireMock.start();

        WebClient webClient = WebClient.builder().build();
        blockersClient = new BlockersClient(webClient);
        ReflectionTestUtils.setField(
                blockersClient,
                "gateway",
                "localhost:" + wireMock.port()
        );
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void sendBlockerRequest_whenBlocked_returnsTrue() {
        String time = "2025-07-12 22:15";

        wireMock.stubFor(post(urlPathMatching("/api/.*/block"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("true")
                )
        );

        Boolean blocked = blockersClient.sendBlockerRequest(time).block();
        assertNotNull(blocked);
        assertTrue(blocked);
    }

    @Test
    void sendBlockerRequest_whenNotBlocked_returnsFalse() {
        String time = "2025-07-12 14:30";

        wireMock.stubFor(post(urlPathMatching("/api/.*/block"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("false")
                )
        );

        Boolean blocked = blockersClient.sendBlockerRequest(time).block();
        assertNotNull(blocked);
        assertFalse(blocked);
    }

    @Test
    void sendBlockerRequest_onServerError_throwsException() {
        String time = "2025-07-12 09:00";

        wireMock.stubFor(post(urlPathMatching("/api/.*/block"))
                .willReturn(aResponse()
                        .withStatus(500)
                )
        );

        assertThrows(Exception.class,
                () -> blockersClient.sendBlockerRequest(time).block()
        );
    }
}
