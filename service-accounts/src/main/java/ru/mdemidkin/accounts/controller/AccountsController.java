package ru.mdemidkin.accounts.controller;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.model.Account;
import ru.mdemidkin.accounts.service.AccountService;
import ru.mdemidkin.accounts.service.UserService;
import ru.mdemidkin.accounts.validation.ValidationUtils;
import ru.mdemidkin.libdto.account.AccountDto;
import ru.mdemidkin.libdto.cash.CashProcessResponse;
import ru.mdemidkin.libdto.cash.CashRequest;
import ru.mdemidkin.libdto.account.Currency;
import ru.mdemidkin.libdto.settings.EditAccountsRequest;
import ru.mdemidkin.libdto.settings.EditPasswordRequest;
import ru.mdemidkin.libdto.signup.SignupRequest;
import ru.mdemidkin.libdto.signup.SignupResponse;
import ru.mdemidkin.libdto.account.UserDto;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class AccountsController {

    private final UserService userService;
    private final AccountService accountService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/api/{login}/user")
    @ResponseBody
    public Mono<UserDto> getUserDto(@PathVariable String login) {
        return userService.findByUsername(login);
    }

    @GetMapping("/api/{login}/accounts")
    @ResponseBody
    public Mono<List<AccountDto>> getUserAccounts(@PathVariable String login) {
        return accountService.getUserAccounts(login)
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

    @GetMapping("/api/users")
    @ResponseBody
    public Mono<List<UserDto>> getAllUsers() {
        return userService.findAllUsers()
                .map(user -> UserDto.builder()
                        .login(user.getLogin())
                        .name(user.getName())
                        .build())
                .collectList();
    }

    @GetMapping("/api/currencies")
    @ResponseBody
    public Mono<List<Currency>> getCurrencies() {
        return Mono.just(List.of(Currency.values()));
    }

    @PostMapping("/api/signup")
    @ResponseBody
    public Mono<SignupResponse> registerNewUser(@RequestBody SignupRequest signupRequest) {
        signupRequest.setDate(LocalDate.parse(signupRequest.getBirthdate(), FORMATTER));
        List<String> errors = ValidationUtils.validateSignupRequest(signupRequest);
        if (errors.isEmpty()) {
            return userService.registerNewUser(signupRequest)
                    .map(user -> buildSignupResponse(signupRequest, null));
        } else {
            return Mono.just(buildSignupResponse(signupRequest, errors));
        }
    }

    @PostMapping("/user/{login}/editPassword")
    public Mono<ResponseEntity<Void>> editPassword(@PathVariable String login,
                                                   @ModelAttribute EditPasswordRequest editPasswordRequest) {
        List<String> passwordErrors = ValidationUtils.validatePasswordRequest(editPasswordRequest);
        if (passwordErrors.isEmpty()) {
            return userService.editPassword(login, editPasswordRequest).then(redirectToMain(login, null, null));
        } else {
            return redirectToMain(login, passwordErrors, null);
        }
    }

    @PostMapping("/user/{login}/editUserAccounts")
    public Mono<ResponseEntity<Void>> editAccounts(@PathVariable String login,
                                                   @ModelAttribute EditAccountsRequest editAccountsRequest) {
        List<String> accounts = editAccountsRequest.getAccount() != null
                ? editAccountsRequest.getAccount()
                : List.of();
        List<String> userAccountsErrors = ValidationUtils.validateEditUserAccountsRequest(editAccountsRequest);
        if (userAccountsErrors.isEmpty()) {
            return userService.updateUserInfo(login, editAccountsRequest)
                    .then(accountService.updateAccounts(login, accounts))
                    .then(redirectToMain(login, null, null));
        } else {
            return redirectToMain(login, null, userAccountsErrors);
        }
    }

    @PostMapping("/api/{login}/cash")
    public Mono<ResponseEntity<CashProcessResponse>> editCash(@PathVariable String login,
                                                              @RequestBody CashRequest cashRequest) {
        return accountService.getAccount(login, cashRequest.getCurrency())
                .flatMap(account -> {
                    List<String> errors = ValidationUtils.validateEditCashRequest(account, cashRequest);
                    if (!errors.isEmpty()) {
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.OK)
                                .body(CashProcessResponse.builder()
                                        .status("error")
                                        .errors(errors)
                                        .build()));
                    }
                    return accountService.updateCashBalance(login, cashRequest)
                            .then(Mono.just(ResponseEntity
                                    .status(HttpStatus.OK)
                                    .body(CashProcessResponse.builder()
                                            .status("completed")
                                            .build())));
                })
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.OK)
                        .body(CashProcessResponse.builder()
                                .status("error")
                                .errors(List.of("Аккаунт не найден " + cashRequest.getCurrency()))
                                .build())));
    }

    @GetMapping("/api/{login}/account/{currency}")
    @ResponseBody
    public Mono<AccountDto> getUserAccount(@PathVariable String login,
                                           @PathVariable String currency) {
        return accountService.getAccount(login, currency)
                .map(account -> AccountDto.builder()
                        .currency(Currency.valueOf(account.getCurrency()))
                        .value(account.getBalance())
                        .exists(true)
                        .build())
                .switchIfEmpty(Mono.just(AccountDto.builder()
                        .exists(false)
                        .build()));
    }

    private Mono<ResponseEntity<Void>> redirectToMain(@NonNull String login,
                                                      @Nullable List<String> passwordErrors,
                                                      @Nullable List<String> userAccountsErrors) {
        URI location = UriComponentsBuilder.fromPath("/")
                .queryParam("login", login)
                .queryParamIfPresent("passwordErrors", Optional.ofNullable(passwordErrors))
                .queryParamIfPresent("userAccountsErrors", Optional.ofNullable(userAccountsErrors))
                .build()
                .toUri();
        return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .build());
    }

    private SignupResponse buildSignupResponse(SignupRequest signupRequest, @Nullable List<String> errors) {
        return SignupResponse.builder()
                .login(signupRequest.getLogin())
                .name(signupRequest.getName())
                .password(signupRequest.getPassword())
                .birthdate(signupRequest.getBirthdate())
                .errors(errors)
                .build();
    }
}
