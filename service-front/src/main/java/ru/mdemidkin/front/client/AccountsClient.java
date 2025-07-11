package ru.mdemidkin.front.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.account.AccountDto;
import ru.mdemidkin.libdto.account.Currency;
import ru.mdemidkin.libdto.account.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountsClient {

    private final WebClient webClient;

    @Value("${services.service-gateway.name}")
    private String gateway;

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

    private <T> Mono<T> sendRequest(String uri, Class<T> responseType, Object... uriVariables) {
        return webClient.get()
                .uri("http://" + gateway + uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
    }

    private <T> Mono<T> sendRequest(String uri, ParameterizedTypeReference<T> responseType, Object... uriVariables) {
        return webClient.get()
                .uri("http://" + gateway + uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
    }

}
