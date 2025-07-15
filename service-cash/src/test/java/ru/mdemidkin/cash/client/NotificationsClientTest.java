package ru.mdemidkin.cash.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.notification.NotificationDto;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationsClientTest {

    private WireMockServer wireMock;
    private NotificationsClient notificationsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        wireMock = new WireMockServer(
                WireMockConfiguration.options()
                        .dynamicPort());
        wireMock.start();

        WebClient webClient = WebClient.builder().build();
        notificationsClient = new NotificationsClient(webClient);
        ReflectionTestUtils.setField(
                notificationsClient,
                "gateway",
                "localhost:" + wireMock.port()
        );

        wireMock.stubFor(post(urlPathEqualTo("/api/joe/notifications"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalTo("Успешная транзакция!"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(objectMapper.writeValueAsString(getNotificationDto()))
                ));
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void sendNotification_whenSuccessful_returnsDto() {
        Mono<NotificationDto> mono = notificationsClient.sendNotification("joe", "Успешная транзакция!");
        NotificationDto result = mono.block();

        assertNotNull(result);
        assertEquals("Успешная транзакция!", result.getMessage());
    }

    @Test
    void sendNotification_whenServerError_throwsException() {
        wireMock.stubFor(post(urlPathEqualTo("/api/jane/notifications"))
                .willReturn(aResponse().withStatus(500)));

        Mono<NotificationDto> mono = notificationsClient.sendNotification("jane", "Test");
        assertThrows(Exception.class, mono::block);
    }

    private NotificationDto getNotificationDto() {
        return NotificationDto.builder()
                .message("Успешная транзакция!")
                .build();
    }
}
