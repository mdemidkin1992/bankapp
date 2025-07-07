package ru.mdemidkin.accounts.exception.advice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.client.FrontUIClient;
import ru.mdemidkin.accounts.exception.SignupRequestException;

@ControllerAdvice
@RequiredArgsConstructor
public class GatewayControllerAdvice {

    private final FrontUIClient frontUIClient;

    @ExceptionHandler
    public Mono<ResponseEntity<String>> handleSignupRequestException(SignupRequestException ex) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", ex.getUser().getName());
        formData.add("login", ex.getUser().getLogin());
        formData.add("password", ex.getUser().getPassword());
        formData.add("birthdate", ex.getUser().getBirthdate().toString());
        ex.getErrors().forEach(error -> formData.add("errors", error));
        return frontUIClient.sendSignupWithErrorsRequest(formData);
    }
}
