package ru.mdemidkin.gateway.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@ControllerAdvice
@RequiredArgsConstructor
public class GatewayControllerAdvice {

    private final WebClient webClient;
    private static final String FRONT_UI_BASE_URL = "http://service-front";

    @ExceptionHandler
    public Mono<ResponseEntity<String>> handleSignupRequestException(SignupRequestException ex) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", ex.getUser().getName());
        formData.add("login", ex.getUser().getLogin());
        formData.add("password", ex.getUser().getPassword());
        formData.add("birthdate", ex.getUser().getBirthdate().toString());
        ex.getErrors().forEach(error -> formData.add("errors", error));

        return webClient.post()
                .uri(FRONT_UI_BASE_URL + "/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .acceptCharset(StandardCharsets.UTF_8)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .toEntity(String.class);
    }
}
