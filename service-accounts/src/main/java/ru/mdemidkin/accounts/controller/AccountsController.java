package ru.mdemidkin.accounts.controller;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.client.FrontUIClient;
import ru.mdemidkin.accounts.dto.EditAccountsRequest;
import ru.mdemidkin.accounts.dto.EditPasswordRequest;
import ru.mdemidkin.accounts.dto.SignupRequest;
import ru.mdemidkin.accounts.model.Account;
import ru.mdemidkin.accounts.service.AccountService;
import ru.mdemidkin.accounts.service.UserService;
import ru.mdemidkin.accounts.validation.ValidSignup;
import ru.mdemidkin.accounts.validation.ValidationUtils;
import ru.mdemidkin.libdto.AccountDto;
import ru.mdemidkin.libdto.Currency;
import ru.mdemidkin.libdto.UserDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@Validated
@RequestMapping
@RequiredArgsConstructor
public class AccountsController {

    private final UserService userService;
    private final AccountService accountService;
    private final FrontUIClient client;

    @GetMapping("/")
    public Mono<ResponseEntity<String>> redirectToMain(ServerWebExchange exchange) {
        return getMainAccountsPage(exchange);
    }

    @GetMapping("/main")
    public Mono<ResponseEntity<String>> getMainAccountsPage(ServerWebExchange exchange) {
        Mono<UserDto> currentUserMono = userService.findByUsername("user1");
        Mono<List<AccountDto>> accountsMono = getAccountsWithAllCurrencies("user1");
        Mono<List<UserDto>> usersMono = getUsers();
        List<Currency> currencies = List.of(Currency.values());
        return Mono.zip(currentUserMono, accountsMono, usersMono)
                .flatMap(tuple -> {
                    UserDto currentUser = tuple.getT1();
                    List<AccountDto> accounts = tuple.getT2();
                    List<UserDto> users = tuple.getT3();
                    return getResponseEntityMono(exchange, currentUser, accounts, users, currencies);
                });
    }

    private Mono<List<AccountDto>> getAccountsWithAllCurrencies(String username) {
        return accountService.getUserAccounts(username)
                .collectList()
                .map(userAccounts -> {
                    Map<String, Account> accountsMap = userAccounts.stream()
                            .collect(Collectors.toMap(
                                    Account::getCurrency,
                                    Function.identity()
                            ));

                    List<AccountDto> allCurrencyAccounts = new ArrayList<>();

                    for (Currency currency : Currency.values()) {
                        Account existingAccount = accountsMap.get(currency.name());

                        if (existingAccount != null) {
                            allCurrencyAccounts.add(AccountDto.builder()
                                    .currency(currency)
                                    .value(existingAccount.getBalance())
                                    .exists(true)
                                    .build());
                        } else {
                            allCurrencyAccounts.add(AccountDto.builder()
                                    .currency(currency)
                                    .value(BigDecimal.ZERO)
                                    .exists(false)
                                    .build());
                        }
                    }

                    return allCurrencyAccounts;
                });
    }

    @NotNull
    private Mono<List<UserDto>> getUsers() {
        return userService.findAllUsers()
                .map(user -> UserDto.builder()
                        .login(user.getLogin())
                        .name(user.getName())
                        .build())
                .collectList();
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
        List<String> errors = ValidationUtils.validatePasswordRequest(editPasswordRequest);
        if (!errors.isEmpty()) {
            return addErrorsToSession(exchange, "passwordErrors", errors);
        }
        return userService.editPassword(login, editPasswordRequest)
                .then(getMainAccountsPage(exchange));
    }

    @PostMapping("/user/{login}/editUserAccounts")
    public Mono<ResponseEntity<String>> editAccounts(@PathVariable String login,
                                                     @ModelAttribute EditAccountsRequest editAccountsRequest,
                                                     ServerWebExchange exchange) {
        List<String> errors = ValidationUtils.validateEditUserAccountsRequest(editAccountsRequest);
        if (!errors.isEmpty()) {
            return addErrorsToSession(exchange, "userAccountsErrors", errors);
        }
        List<String> accounts = editAccountsRequest.getAccount() != null ? editAccountsRequest.getAccount() : List.of();
        return userService.updateUserInfo(login, editAccountsRequest)
                .then(accountService.updateAccounts(login, accounts))
                .then(getMainAccountsPage(exchange));
    }

    private Mono<ResponseEntity<String>> addErrorsToSession(ServerWebExchange exchange,
                                                            String field,
                                                            List<String> errors) {
        return exchange.getSession()
                .doOnNext(session -> session.getAttributes().put(field, errors))
                .then(getMainAccountsPage(exchange));
    }

    private Mono<ResponseEntity<String>> getResponseEntityMono(ServerWebExchange exchange,
                                                               UserDto currentUser,
                                                               List<AccountDto> accounts,
                                                               List<UserDto> users,
                                                               List<Currency> currencies) {
        return exchange.getSession()
                .flatMap(session ->
                        client.sendMainPageRequest(
                                session,
                                currentUser,
                                accounts,
                                users,
                                currencies
                        )
                );
    }
}
