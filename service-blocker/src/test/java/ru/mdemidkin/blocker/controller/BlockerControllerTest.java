package ru.mdemidkin.blocker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.blocker.service.BlockerService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(BlockerController.class)
@WithMockUser
class BlockerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BlockerService blockerService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @BeforeEach
    void init() {
        this.webTestClient = webTestClient.mutateWith(csrf());
    }

    @Test
    void postUserNotification_returnsTrue() {
        // given
        String time = "2025-07-12 17:00";
        String encoded = URLEncoder.encode(time, StandardCharsets.UTF_8);
        LocalDateTime parsed = LocalDateTime.parse(time, FORMATTER);
        when(blockerService.isBlocked(parsed)).thenReturn(Mono.just(true));

        // when / then
        webTestClient.post()
                .uri(uri -> uri
                        .path("/api/{time}/block")
                        .build(time)
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class).isEqualTo(true);
    }

    @Test
    void postUserNotification_returnsFalse() {
        // given
        String time = "2025-07-13 09:30";
        String encoded = URLEncoder.encode(time, StandardCharsets.UTF_8);
        LocalDateTime parsed = LocalDateTime.parse(time, FORMATTER);
        when(blockerService.isBlocked(parsed)).thenReturn(Mono.just(false));

        // when / then
        webTestClient.post()
                .uri(uri -> uri
                        .path("/api/{time}/block")
                        .build(time)
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class).isEqualTo(false);
    }

    @Test
    void postUserNotification_serviceError() {
        // given
        String time = "2025-07-14 12:45";
        String encoded = URLEncoder.encode(time, StandardCharsets.UTF_8);
        LocalDateTime parsed = LocalDateTime.parse(time, FORMATTER);
        when(blockerService.isBlocked(parsed)).thenReturn(Mono.error(new RuntimeException("failed")));

        // when / then
        webTestClient.post()
                .uri(uri -> uri
                        .path("/api/{time}/block")
                        .build(time)
                )
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
