package ru.mdemidkin.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mdemidkin.gateway.client.AccountsClient;
import ru.mdemidkin.libdto.signup.SignupRequest;

@Controller
@RequiredArgsConstructor
public class SignupController {

    private final AccountsClient accountsClient;
    private final ReactiveAuthenticationManager authenticationManager;
    private final ServerSecurityContextRepository securityContextRepository;

    @GetMapping("/signup")
    public Mono<String> getSignupPage() {
        return Mono.just("signup");
    }

    @PostMapping("/signup")
    public Mono<String> registerNewUser(@ModelAttribute SignupRequest signupRequest,
                                        ServerWebExchange exchange,
                                        Model model) {
        return accountsClient.signup(signupRequest)
                .flatMap(response -> {
                    if (response.getErrors() != null) {
                        model.addAttribute("name", response.getName());
                        model.addAttribute("login", response.getLogin());
                        model.addAttribute("password", response.getPassword());
                        model.addAttribute("birthdate", response.getBirthdate());
                        model.addAttribute("errors", response.getErrors());
                        return Mono.just("signup");
                    }

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            response.getLogin(),
                            response.getPassword()
                    );

                    return authenticationManager.authenticate(auth)
                            .flatMap(authenticated -> {
                                SecurityContext context = new SecurityContextImpl(authenticated);
                                return securityContextRepository.save(exchange, context)
                                        .then(exchange.getSession())
                                        .thenReturn("redirect:/");
                            });
                });
    }
}
