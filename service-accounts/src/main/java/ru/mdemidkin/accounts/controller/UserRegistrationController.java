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
//                    return userService.findByUsername(principal.getName())
                            .flatMap(userData -> client.sendMainPageRequest(session, userData, accounts, users, currencies));
                });
    }

    @PostMapping("/signup/render")
    public Mono<String> registerNewUser(@ModelAttribute @ValidSignup SignupRequest signupRequest) {
        return userService.registerNewUser(signupRequest)
                .then(Mono.just("redirect:/main"));
    }

    @PostMapping("/user/{login}/editPassword")
    public Mono<String> editPassword(@PathVariable String login,
                                     @ModelAttribute @ValidPassword EditPasswordRequest editPasswordRequest,
                                     ServerWebExchange exchange) {
        return userService.editPassword(login, editPasswordRequest)
                .then(Mono.just("redirect:/main"))
                .onErrorResume(EditPasswordRequestException.class, ex -> exchange.getSession()
                        .doOnNext(session -> session.getAttributes().put("passwordErrors", ex.getErrors()))
                        .then(Mono.just("redirect:/main")));
    }

    private void addModelAttributes(
            WebSession session,
            Model model,
            UserDto userData,
            List<AccountDto> accounts,
            List<UserDto> users
    ) {
        model.addAttribute("login", userData.getLogin());
        model.addAttribute("name", userData.getName());
        model.addAttribute("birthdate", userData.getBirthdate());
        model.addAttribute("accounts", accounts);
        model.addAttribute("users", users);
        model.addAttribute("passwordErrors", session.getAttribute("passwordErrors"));
        model.addAttribute("userAccountsErrors", session.getAttribute("userAccountsErrors"));
        model.addAttribute("cashErrors", session.getAttribute("cashErrors"));
        model.addAttribute("transferErrors", session.getAttribute("transferErrors"));
        model.addAttribute("transferOtherErrors", session.getAttribute("transferOtherErrors"));
    }
}
