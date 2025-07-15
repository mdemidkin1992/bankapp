package ru.mdemidkin.gateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.account.UserDto;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {

    private final WebClient accountsClient;
    private static final String ACCOUNTS_BASE_URL = "http://service-accounts";

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return accountsClient
                .get()
                .uri(ACCOUNTS_BASE_URL + "/auth/users/{username}", username)
                .retrieve()
                .bodyToMono(UserDto.class)
                .map(this::mapToUserDetails);
    }

    private UserDetails mapToUserDetails(UserDto userDto) {
        return new org.springframework.security.core.userdetails.User(
                userDto.getLogin(),
                userDto.getPassword(),
                List.of(new SimpleGrantedAuthority(userDto.getRole())));
    }
}
