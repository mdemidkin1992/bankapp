package ru.mdemidkin.front.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.front.client.AccountsClient;
import ru.mdemidkin.libdto.account.AccountDto;
import ru.mdemidkin.libdto.account.Currency;
import ru.mdemidkin.libdto.account.UserDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(FrontEndController.class)
@WithMockUser
class FrontEndControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AccountsClient accountsClient;

    @BeforeEach
    void init() {
        this.webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    void setUp() {
        UserDto user = UserDto.builder()
                .login("user1")
                .name("Test User")
                .birthdate(LocalDate.of(1990, 1, 1))
                .build();
        when(accountsClient.getUserDto(any())).thenReturn(Mono.just(user));
        when(accountsClient.getAccountsList(any()))
                .thenReturn(Mono.just(List.of(
                        AccountDto.builder().currency(Currency.USD).value(new BigDecimal("100.00")).exists(true).build(),
                        AccountDto.builder().currency(Currency.CNY).value(new BigDecimal("50.00")).exists(true).build()
                )));
        when(accountsClient.getUsers())
                .thenReturn(Mono.just(List.of(
                        UserDto.builder().login("user2").name("Another").build()
                )));
        when(accountsClient.getCurrencies())
                .thenReturn(Mono.just(List.of(
                        Currency.USD,
                        Currency.CNY
                )));
    }

    @Test
    void redirectToMain_withParameters_shouldRedirectToMain() {
        webTestClient.get()
                .uri(uri -> uri.path("/")
                        .queryParam("login", "user1")
                        .queryParam("passwordErrors", "pwErr")
                        .queryParam("cashErrors", "cErr")
                        .queryParam("transferOtherErrors", "toErr")
                        .queryParam("transferErrors", "tErr")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main");
    }

    @Test
    void getMainPage_withoutSessionLogin_shouldUseHeader() {
        webTestClient.get()
                .uri("/main")
                .header("X-User-Login", "user1")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class);
    }

    @Test
    void getMainPage_withSessionLogin_shouldUseSessionAttribute() {
        webTestClient.get()
                .uri(uri -> uri.path("/")
                        .queryParam("login", "user1")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();

        webTestClient.get()
                .uri("/main")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class);
    }
}
