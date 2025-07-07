package ru.mdemidkin.accounts.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.AccountDto;
import ru.mdemidkin.libdto.Currency;
import ru.mdemidkin.libdto.MainPageRequest;
import ru.mdemidkin.libdto.UserDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FrontUIClient {
    private final WebClient webClient;
    private static final String FRONT_UI_BASE_URL = "http://service-front";

    public Mono<ResponseEntity<String>> sendSignupWithErrorsRequest(MultiValueMap<String, String> formData) {
        return webClient.post()
                .uri(FRONT_UI_BASE_URL + "/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .acceptCharset(StandardCharsets.UTF_8)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .toEntity(String.class);
    }

    public Mono<ResponseEntity<String>> sendMainPageRequest(WebSession session,
                                                            UserDto userData,
                                                            List<AccountDto> accounts,
                                                            List<UserDto> users,
                                                            List<Currency> currency
    ) {
        MainPageRequest request = new MainPageRequest(
                userData.getLogin(),
                userData.getName(),
                userData.getBirthdate(),
                accounts,
                users,
                currency,
                session.getAttribute("passwordErrors"),
                session.getAttribute("userAccountsErrors"),
                session.getAttribute("cashErrors"),
                session.getAttribute("transferErrors"),
                session.getAttribute("transferOtherErrors")
        );

        return webClient.post()
                .uri(FRONT_UI_BASE_URL + "/main")
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(request)
                .retrieve()
                .toEntity(String.class);
    }
}
