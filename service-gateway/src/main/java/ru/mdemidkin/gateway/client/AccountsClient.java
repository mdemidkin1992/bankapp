package ru.mdemidkin.gateway.client;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.account.AccountDto;
import ru.mdemidkin.libdto.account.Currency;
import ru.mdemidkin.libdto.account.UserDto;
import ru.mdemidkin.libdto.signup.SignupRequest;
import ru.mdemidkin.libdto.signup.SignupResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountsClient {
    private final WebClient webClient;
    private static final String ACCOUNTS_BASE_URL = "http://service-accounts";

    public Mono<UserDto> getUserDto(String login) {
        return sendRequest("/api/{login}/user", UserDto.class, login);
    }

    public Mono<List<AccountDto>> getAccountsList(String login) {
        return sendRequest("/api/{login}/accounts", new ParameterizedTypeReference<>() {
        }, login);
    }

    public Mono<List<UserDto>> getUsers() {
        return sendRequest("/api/users", new ParameterizedTypeReference<>() {
        });
    }


    public Mono<List<Currency>> getCurrencies() {
        return sendRequest("/api/currencies", new ParameterizedTypeReference<>() {
        });
    }

    public Mono<SignupResponse> signup(SignupRequest signupRequest) {
        return webClient.post()
                .uri(ACCOUNTS_BASE_URL + "/api/signup")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(signupRequest)
                .retrieve()
                .bodyToMono(SignupResponse.class);
    }

    private <T> Mono<T> sendRequest(String uri, Class<T> responseType, Object... uriVariables) {
        return webClient.get()
                .uri(ACCOUNTS_BASE_URL + uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
    }

    private <T> Mono<T> sendRequest(String uri, ParameterizedTypeReference<T> responseType, Object... uriVariables) {
        return webClient.get()
                .uri(ACCOUNTS_BASE_URL + uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
    }

}
