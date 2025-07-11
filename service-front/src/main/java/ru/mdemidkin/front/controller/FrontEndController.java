package ru.mdemidkin.front.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.mdemidkin.front.client.AccountsClient;
import ru.mdemidkin.libdto.account.AccountDto;
import ru.mdemidkin.libdto.account.Currency;
import ru.mdemidkin.libdto.account.UserDto;

import java.util.List;

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class FrontEndController {

    private final AccountsClient accountsClient;

    @Value("${headers.user-header}")
    private String userHeader;

    @GetMapping("/")
    public Mono<String> redirectToMain(ServerWebExchange exchange,
                                       @RequestParam(required = false) String login,
                                       @RequestParam(required = false) String passwordErrors,
                                       @RequestParam(required = false) String cashErrors,
                                       @RequestParam(required = false) String transferOtherErrors,
                                       @RequestParam(required = false) String transferErrors) {
        return exchange.getSession()
                .flatMap(session -> {
                    if (login != null) {
                        session.getAttributes().put("login", login);
                    }
                    if (passwordErrors != null) {
                        session.getAttributes().put("passwordErrors", passwordErrors);
                    }
                    if (cashErrors != null) {
                        session.getAttributes().put("cashErrors", cashErrors);
                    }
                    if (transferOtherErrors != null) {
                        session.getAttributes().put("transferOtherErrors", transferOtherErrors);
                    }
                    if (transferErrors != null) {
                        session.getAttributes().put("transferErrors", transferErrors);
                    }
                    return Mono.just("redirect:/main");
                });
    }

    @GetMapping("/main")
    public Mono<String> getMainPage(ServerWebExchange exchange,
                                    Model model) {
        String loginAttribute = exchange.getAttribute("login");
        String identifiedLogin = identifyUsername(loginAttribute, exchange);

        log.info("exchange.getAttribute {}, identifiedLogin {}, header {}", loginAttribute, identifiedLogin,
                exchange.getRequest().getHeaders().getFirst("X-User-Login"));

        Mono<UserDto> currentUserMono = accountsClient.getUserDto(identifiedLogin);
        Mono<List<AccountDto>> accountsMono = accountsClient.getAccountsList(identifiedLogin);
        Mono<List<UserDto>> usersMono = accountsClient.getUsers();
        Mono<List<Currency>> currenciesMono = accountsClient.getCurrencies();

        return Mono.zip(currentUserMono, accountsMono, usersMono, currenciesMono)
                .flatMap(zipped -> {
                    UserDto currentUser = zipped.getT1();
                    List<AccountDto> accounts = zipped.getT2();
                    List<UserDto> users = zipped.getT3();
                    List<Currency> currencies = zipped.getT4();

                    return exchange.getSession()
                            .map(session -> {
                                assignModelAttributes(model, session, currentUser, accounts, users, currencies);
                                clearSessions(session);
                                return "main";
                            });
                });
    }

    private void assignModelAttributes(Model model,
                                       WebSession session,
                                       UserDto currentUser,
                                       List<AccountDto> accounts,
                                       List<UserDto> users,
                                       List<Currency> currencies) {
        model.addAttribute("login", currentUser.getLogin());
        model.addAttribute("name", currentUser.getName());
        model.addAttribute("birthdate", currentUser.getBirthdate());
        model.addAttribute("accounts", accounts);
        model.addAttribute("users", users);
        model.addAttribute("currency", currencies);
        model.addAttribute("passwordErrors", session.getAttribute("passwordErrors"));
        model.addAttribute("userAccountsErrors", session.getAttribute("userAccountsErrors"));
        model.addAttribute("cashErrors", session.getAttribute("cashErrors"));
        model.addAttribute("transferErrors", session.getAttribute("transferErrors"));
        model.addAttribute("transferOtherErrors", session.getAttribute("transferOtherErrors"));
    }

    private void clearSessions(WebSession session) {
        session.getAttributes().remove("passwordErrors");
        session.getAttributes().remove("userAccountsErrors");
        session.getAttributes().remove("cashErrors");
        session.getAttributes().remove("transferErrors");
        session.getAttributes().remove("transferOtherErrors");
    }

    /**
     * Определяет пользователя. При авторизации/регистрации передается в Principal.
     * При редиректе из других сервисов определяет по параметру "login" в Session.
     */
    private String identifyUsername(String loginAttribute,
                                    ServerWebExchange exchange) {
        if (loginAttribute != null && !loginAttribute.isBlank()) {
            return loginAttribute;
        }

        return exchange.getRequest().getHeaders().getFirst(userHeader);
    }

}
