package ru.mdemidkin.accounts.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.service.UserService;
import ru.mdemidkin.libdto.account.UserDto;

import java.time.LocalDate;

import static org.mockito.Mockito.when;

@WebFluxTest(UserAuthController.class)
@WithMockUser
class UserAuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    @Test
    void getUserByUsername_success() {
        UserDto dto = UserDto.builder()
                .login("user1")
                .name("Test User")
                .role("USER")
                .birthdate(LocalDate.of(1990, 1, 1))
                .build();

        when(userService.findByUsername("user1")).thenReturn(Mono.just(dto));

        webTestClient.get()
                .uri("/auth/users/user1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.login").isEqualTo("user1")
                .jsonPath("$.name").isEqualTo("Test User")
                .jsonPath("$.role").isEqualTo("USER")
                .jsonPath("$.birthdate").isEqualTo("1990-01-01");
    }

    @Test
    void getUserByUsername_notFound() {
        when(userService.findByUsername("unknown"))
                .thenReturn(Mono.error(new UsernameNotFoundException("User not found")));

        webTestClient.get()
                .uri("/auth/users/unknown")
                .exchange()
                .expectStatus().isFound();
    }
}
