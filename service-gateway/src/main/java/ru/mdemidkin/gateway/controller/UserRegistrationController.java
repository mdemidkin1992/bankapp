package ru.mdemidkin.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.mdemidkin.gateway.dto.SignupRequest;
import ru.mdemidkin.gateway.service.UserService;
import ru.mdemidkin.gateway.validation.ValidSignup;

@Controller
@Validated
@RequestMapping
@RequiredArgsConstructor
public class UserRegistrationController {

    private final UserService userService;

    @PostMapping("/signup/render")
    public Mono<String> registerNewUser(@ModelAttribute @ValidSignup SignupRequest signupRequest) {
        return userService.registerNewUser(signupRequest)
                .then(Mono.just("redirect:/main"));
    }

}
