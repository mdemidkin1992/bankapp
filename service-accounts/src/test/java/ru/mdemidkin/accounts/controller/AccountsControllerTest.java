package ru.mdemidkin.accounts.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.model.Account;
import ru.mdemidkin.accounts.model.User;
import ru.mdemidkin.accounts.service.AccountService;
import ru.mdemidkin.accounts.service.UserService;
import ru.mdemidkin.accounts.validation.ValidationUtils;
import ru.mdemidkin.libdto.account.AccountDto;
import ru.mdemidkin.libdto.account.Currency;
import ru.mdemidkin.libdto.account.UserDto;
import ru.mdemidkin.libdto.cash.CashAction;
import ru.mdemidkin.libdto.cash.CashProcessResponse;
import ru.mdemidkin.libdto.cash.CashRequest;
import ru.mdemidkin.libdto.settings.EditAccountsRequest;
import ru.mdemidkin.libdto.settings.EditPasswordRequest;
import ru.mdemidkin.libdto.signup.SignupRequest;
import ru.mdemidkin.libdto.signup.SignupResponse;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(controllers = AccountsController.class)
@WithMockUser
class AccountsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    @MockBean
    private AccountService accountService;

    @BeforeEach
    void init() {
        this.webTestClient = webTestClient.mutateWith(csrf());
    }

    @Test
    void shouldGetUserDto() {
        UserDto userDto = UserDto.builder()
                .login("testuser")
                .name("Test User")
                .build();

        when(userService.findByUsername("testuser"))
                .thenReturn(Mono.just(userDto));

        webTestClient.get()
                .uri("/api/testuser/user")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .isEqualTo(userDto);
    }

    @Test
    void shouldGetUserAccounts() {
        Account usdAccount = createTestAccount("USD", new BigDecimal("100.00"));
        Account eurAccount = createTestAccount("EUR", new BigDecimal("50.00"));

        when(accountService.getUserAccounts("testuser"))
                .thenReturn(Flux.just(usdAccount, eurAccount));

        webTestClient.get()
                .uri("/api/testuser/accounts")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Account.class)
                .hasSize(3);
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = createTestUser("user1", "User One");
        User user2 = createTestUser("user2", "User Two");

        when(userService.findAllUsers())
                .thenReturn(Flux.just(user1, user2));

        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].login").isEqualTo("user1")
                .jsonPath("$[0].name").isEqualTo("User One")
                .jsonPath("$[1].login").isEqualTo("user2")
                .jsonPath("$[1].name").isEqualTo("User Two");
    }

    @Test
    void shouldGetCurrencies() {
        webTestClient.get()
                .uri("/api/currencies")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Currency.class)
                .hasSize(3);
    }

    @Test
    void shouldRegisterNewUser() {
        SignupRequest signupRequest = SignupRequest.builder()
                .login("newuser")
                .name("New User")
                .password("password123")
                .confirmPassword("password123")
                .birthdate("1990-01-01")
                .build();

        User newUser = createTestUser("newuser", "New User");

        try (MockedStatic<ValidationUtils> utilities = mockStatic(ValidationUtils.class)) {
            utilities.when(() -> ValidationUtils.validateSignupRequest(any()))
                    .thenReturn(List.of());

            when(userService.registerNewUser(any(SignupRequest.class)))
                    .thenReturn(Mono.just(newUser));

            webTestClient.post()
                    .uri("/api/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(signupRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(SignupResponse.class)
                    .value(response -> {
                        assertEquals("newuser", response.getLogin());
                        assertEquals("New User", response.getName());
                        assertNull(response.getErrors());
                    });
        }
    }

    @Test
    void shouldReturnErrorsOnInvalidSignup() {
        SignupRequest signupRequest = SignupRequest.builder()
                .login("user")
                .name("User")
                .password("pass")
                .birthdate("2020-01-01")
                .build();

        List<String> errors = Arrays.asList(
                "Повторный пароль не должен быть пустым",
                "Пароли должны совпадать", "Возраст должен быть старше 18 лет");

        try (MockedStatic<ValidationUtils> utilities = mockStatic(ValidationUtils.class)) {
            utilities.when(() -> ValidationUtils.validateSignupRequest(any()))
                    .thenReturn(errors);

            webTestClient.post()
                    .uri("/api/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(signupRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(SignupResponse.class)
                    .value(response -> {
                        assertEquals(errors, response.getErrors());
                    });
        }
    }

    @Test
    void shouldEditPassword() {
        EditPasswordRequest request = new EditPasswordRequest();
        request.setPassword("newpass123");
        request.setConfirmPassword("newpass123");

        try (MockedStatic<ValidationUtils> utilities = mockStatic(ValidationUtils.class)) {
            utilities.when(() -> ValidationUtils.validatePasswordRequest(any()))
                    .thenReturn(List.of());

            when(userService.editPassword(eq("testuser"), any(EditPasswordRequest.class)))
                    .thenReturn(Mono.empty());

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("password", "newpass123");
            formData.add("confirmPassword", "newpass123");

            webTestClient.post()
                    .uri("/user/testuser/editPassword")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .exchange()
                    .expectStatus().is3xxRedirection()
                    .expectHeader().location("/?login=testuser");
        }
    }

    @Test
    void shouldEditPasswordWithErrors() {
        EditPasswordRequest request = new EditPasswordRequest();
        request.setPassword("old");
        request.setConfirmPassword("new");

        List<String> errors = List.of("Пароли должны совпадать.");

        try (MockedStatic<ValidationUtils> utilities = mockStatic(ValidationUtils.class)) {
            utilities.when(() -> ValidationUtils.validatePasswordRequest(any()))
                    .thenReturn(errors);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("password", "old");
            formData.add("confirmPassword", "new");

            webTestClient.post()
                    .uri("/user/testuser/editPassword")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .exchange()
                    .expectStatus().is3xxRedirection();
        }
    }

    @Test
    void shouldEditUserAccounts() {
        EditAccountsRequest request = new EditAccountsRequest();
        request.setName("Updated Name");
        request.setAccount(Arrays.asList("USD", "EUR"));

        try (MockedStatic<ValidationUtils> utilities = mockStatic(ValidationUtils.class)) {
            utilities.when(() -> ValidationUtils.validateEditUserAccountsRequest(any()))
                    .thenReturn(List.of());

            when(userService.updateUserInfo(eq("testuser"), any(EditAccountsRequest.class)))
                    .thenReturn(Mono.empty());
            when(accountService.updateAccounts(eq("testuser"), anyList()))
                    .thenReturn(Mono.empty());

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("name", "Updated Name");
            formData.addAll("account", Arrays.asList("USD", "EUR"));

            webTestClient.post()
                    .uri("/user/testuser/editUserAccounts")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .exchange()
                    .expectStatus().is3xxRedirection()
                    .expectHeader().location("/?login=testuser");
        }
    }

    @Test
    void shouldEditCash() {
        CashRequest cashRequest = new CashRequest();
        cashRequest.setCurrency("USD");
        cashRequest.setValue("50.00");
        cashRequest.setAction(CashAction.PUT);

        Account account = createTestAccount("USD", new BigDecimal("100.00"));

        try (MockedStatic<ValidationUtils> utilities = mockStatic(ValidationUtils.class)) {
            utilities.when(() -> ValidationUtils.validateEditCashRequest(any(), any()))
                    .thenReturn(List.of());

            when(accountService.getAccount("testuser", "USD"))
                    .thenReturn(Mono.just(account));
            when(accountService.updateCashBalance(eq("testuser"), any(CashRequest.class)))
                    .thenReturn(Mono.empty());

            webTestClient.post()
                    .uri("/api/testuser/cash")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(cashRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CashProcessResponse.class)
                    .value(response -> {
                        assertEquals("completed", response.getStatus());
                    });
        }
    }

    @Test
    void shouldReturnErrorWhenAccountNotFound() {
        CashRequest cashRequest = new CashRequest();
        cashRequest.setCurrency("GBP");
        cashRequest.setValue("50.00");
        cashRequest.setAction(CashAction.PUT);

        when(accountService.getAccount("testuser", "GBP"))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/testuser/cash")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cashRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CashProcessResponse.class)
                .value(response -> {
                    assertEquals("error", response.getStatus());
                    assertTrue(response.getErrors().contains("Аккаунт не найден GBP"));
                });
    }

    @Test
    void shouldReturnErrorsOnInvalidCashRequest() {
        CashRequest cashRequest = new CashRequest();
        cashRequest.setCurrency("USD");
        cashRequest.setValue("1000.00");
        cashRequest.setAction(CashAction.GET);

        Account account = createTestAccount("USD", new BigDecimal("100.00"));
        List<String> errors = List.of("Не достаточно средств для снятия USD 1000.00");

        try (MockedStatic<ValidationUtils> utilities = mockStatic(ValidationUtils.class)) {
            utilities.when(() -> ValidationUtils.validateEditCashRequest(any(), any()))
                    .thenReturn(errors);

            when(accountService.getAccount("testuser", "USD"))
                    .thenReturn(Mono.just(account));

            webTestClient.post()
                    .uri("/api/testuser/cash")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(cashRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CashProcessResponse.class)
                    .value(response -> {
                        assertEquals("error", response.getStatus());
                        assertEquals(errors, response.getErrors());
                    });
        }
    }

    @Test
    void shouldGetUserAccount() {
        Account account = createTestAccount("USD", new BigDecimal("100.00"));

        when(accountService.getAccount("testuser", "USD"))
                .thenReturn(Mono.just(account));

        webTestClient.get()
                .uri("/api/testuser/account/USD")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountDto.class)
                .value(dto -> {
                    assertEquals(Currency.USD, dto.getCurrency());
                    assertEquals(0, dto.getValue().compareTo(new BigDecimal("100.00")));
                    assertTrue(dto.isExists());
                });
    }

    @Test
    void shouldReturnNonExistentAccountDto() {
        when(accountService.getAccount("testuser", "GBP"))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/testuser/account/GBP")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountDto.class)
                .value(dto -> {
                    assertFalse(dto.isExists());
                });
    }

    private Account createTestAccount(String currency, BigDecimal balance) {
        Account account = new Account();
        account.setCurrency(currency);
        account.setBalance(balance);
        return account;
    }

    private User createTestUser(String login, String name) {
        User user = new User();
        user.setLogin(login);
        user.setName(name);
        return user;
    }
}