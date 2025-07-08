package ru.mdemidkin.accounts.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.client.FrontUIClient;
import ru.mdemidkin.accounts.dto.EditPasswordRequest;
import ru.mdemidkin.accounts.dto.SignupRequest;
import ru.mdemidkin.accounts.exception.EditPasswordRequestException;
import ru.mdemidkin.accounts.service.UserService;
import ru.mdemidkin.accounts.validation.ValidPassword;
import ru.mdemidkin.accounts.validation.ValidSignup;
import ru.mdemidkin.libdto.AccountDto;
import ru.mdemidkin.libdto.Currency;
import ru.mdemidkin.libdto.UserDto;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@Validated
@RequestMapping
@RequiredArgsConstructor
public class UserRegistrationController {

    private final UserService userService;
    private final FrontUIClient client;

    @GetMapping("/")
    public Mono<ResponseEntity<String>> redirectToMain(ServerWebExchange exchange) {
        return getMainAccountsPage(exchange);
    }

    @GetMapping("/main")
    public Mono<ResponseEntity<String>> getMainAccountsPage(ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    List<AccountDto> accounts = List.of(AccountDto.builder() // todo
                            .currency(Currency.RUB)
                            .value(100.0)
                            .exists(true)
                            .build());

                    List<UserDto> users = List.of(UserDto.builder() // todo
                            .name("name")
                            .login("login")
                            .build());

                    List<Currency> currencies = List.of(Currency.values()); // todo

                    return userService.findByUsername("user1")
                            .flatMap(userData -> client.sendMainPageRequest(session,
                                    userData, accounts, users, currencies));
                });
    }

    @PostMapping("/signup/render")
    public Mono<ResponseEntity<String>> registerNewUser(@ModelAttribute @ValidSignup SignupRequest signupRequest,
                                        ServerWebExchange exchange) {
        return userService.registerNewUser(signupRequest)
                .then(getMainAccountsPage(exchange));
    }

    @PostMapping("/user/{login}/editPassword")
    public Mono<ResponseEntity<String>> editPassword(@PathVariable String login,
                                     @ModelAttribute EditPasswordRequest editPasswordRequest,
                                     ServerWebExchange exchange) {
        List<String> errors = validatePasswordRequest(editPasswordRequest);
        if (!errors.isEmpty()) {
            return exchange.getSession()
                    .doOnNext(session -> session.getAttributes().put("passwordErrors", errors))
                    .then(getMainAccountsPage(exchange));
        }
        return userService.editPassword(login, editPasswordRequest)
                .then(getMainAccountsPage(exchange));
    }

    private List<String> validatePasswordRequest(EditPasswordRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            errors.add("Пароль не должен быть пустым");
        }
        if (request.getConfirmPassword() == null || request.getConfirmPassword().isBlank()) {
            errors.add("Повторный пароль не должен быть пустым");
        }
        if (request.getPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            errors.add("Пароли должны совпадать");
        }

        return errors;
    }
}
