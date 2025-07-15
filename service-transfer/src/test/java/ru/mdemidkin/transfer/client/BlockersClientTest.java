package ru.mdemidkin.transfer.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockersClientTest {

    private WireMockServer wireMockServer;
    private BlockersClient blockersClient;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.options().dynamicPort()
        );
        wireMockServer.start();

        WebClient webClient = WebClient.builder().build();
        blockersClient = new BlockersClient(webClient);
        ReflectionTestUtils.setField(
                blockersClient,
                "gateway",
                "localhost:" + wireMockServer.port()
        );
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void sendBlockerRequest_whenBlocked_returnsTrue() {
        wireMockServer.stubFor(post(urlPathMatching("/api/.*/block"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("true")
                )
        );

        Boolean result = blockersClient
                .sendBlockerRequest("2025-07-13 22:00")
                .block();

        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    void sendBlockerRequest_whenNotBlocked_returnsFalse() {
        wireMockServer.stubFor(post(urlPathMatching("/api/.*/block"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("false")
                )
        );

        Boolean result = blockersClient
                .sendBlockerRequest("2025-07-14 12:00")
                .block();

        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    void sendBlockerRequest_onServerError_throwsException() {
        wireMockServer.stubFor(post(urlPathMatching("/api/.*/block"))
                .willReturn(aResponse()
                        .withStatus(500)
                )
        );

        Mono<Boolean> mono = blockersClient.sendBlockerRequest("2025-07-15 09:00");
        assertThrows(Exception.class, mono::block);
    }
}
